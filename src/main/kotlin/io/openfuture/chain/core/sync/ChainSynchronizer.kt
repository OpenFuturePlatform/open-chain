package io.openfuture.chain.core.sync

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.DBChecker
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.service.block.validation.MainBlockValidator
import io.openfuture.chain.core.service.block.validation.pipeline.BlockValidationPipeline
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceSchemaCreatedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import javax.annotation.PostConstruct

@Component
class ChainSynchronizer(
    private val consensusProperties: ConsensusProperties,
    private val addressesHolder: AddressesHolder,
    private val blockManager: BlockManager,
    private val transactionManager: TransactionManager,
    private val networkApiService: NetworkApiService,
    private val mainBlockValidator: MainBlockValidator,
    private val epochService: EpochService,
    private val requestRetryScheduler: RequestRetryScheduler,
    private val dbChecker: DBChecker,
    private val nodeConfigurator: NodeConfigurator,
    private val syncSession: SyncSession
) : ApplicationListener<DataSourceSchemaCreatedEvent> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    @Volatile private var status: SyncStatus = SYNCHRONIZED
    private var future: ScheduledFuture<*>? = null


    @PostConstruct
    fun forceSynchronizationCheck() {
        checkLastBlock()
    }

    override fun onApplicationEvent(event: DataSourceSchemaCreatedEvent) {
        prepareDB(nodeConfigurator.getConfig().mode)
    }

    @EventListener
    fun eventSyncMode(syncMode: SyncMode) {
        nodeConfigurator.setMode(syncMode)
        prepareDB(syncMode)
    }

    fun prepareDB(syncMode: SyncMode) {
        status = PROCESSING
        dbChecker.prepareDB(syncMode)
        status = SYNCHRONIZED
    }

    fun getStatus(): SyncStatus = status

    fun onEpochResponse(message: EpochResponseMessage) {
        future?.cancel(true)
        val delegates = blockManager.getLastGenesisBlock().getPayload().activeDelegates
        try {

            if (!message.isEpochExists ||
                (syncSession.getCurrentGenesisBlock().getPayload().epochIndex > message.genesisBlock!!.epochIndex
                    && message.mainBlocks.isEmpty())) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if (!syncSession.add(convertToBlocks(message))) {
                log.warn("Epoch #${message.genesisBlock!!.epochIndex} is invalid, requesting another node...")
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if (!syncSession.isCompleted()) {
                requestEpoch(delegates)
                return
            }

            saveBlocks()
            clearUnconfirmedTransactions()
        } catch (e: Throwable) {
            log.error(e.message)
            syncFailed()
        }
    }

    fun isInSync(block: Block): Boolean {
        val lastBlock = blockManager.getLast()
        val lastMainBlock = if (lastBlock !is MainBlock) blockManager.getLastMainBlock() else lastBlock
        if (lastBlock.hash == block.hash) {
            return true
        }
        val handlers = arrayOf(mainBlockValidator.checkHeight(), mainBlockValidator.checkPreviousHash())
        val pipeline = BlockValidationPipeline(handlers)
        return mainBlockValidator.verify(block, lastBlock, lastMainBlock, false, pipeline)
    }

    @Synchronized
    fun checkLastBlock() {
        log.debug("Chain in status=$status")
        if (PROCESSING == status) {
            return
        }
        status = PROCESSING
        val block = blockManager.getLast()
        checkBlock(block)
        future = requestRetryScheduler.startRequestScheduler(future, Runnable { checkBlock(block) })
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        future?.cancel(true)
        if (-1L == response.height) {
            val invalidGenesisBlock = blockManager.getLastGenesisBlock()
            log.info("Rolling back epoch #${invalidGenesisBlock.getPayload().epochIndex}")
            blockManager.removeEpoch(invalidGenesisBlock)
            val lastGenesisBlock = blockManager.getLastGenesisBlock()
            val requestedBlock = if (1L == lastGenesisBlock.height) {
                blockManager.getLast()
            } else {
                lastGenesisBlock
            }
            checkBlock(requestedBlock)
            future = requestRetryScheduler.startRequestScheduler(future, Runnable { checkBlock(lastGenesisBlock) })
        } else {
            val lastGenesisBlock = response.genesisBlock ?: blockManager.getLastGenesisBlock().toMessage()
            initSync(lastGenesisBlock)
        }
    }

    private fun initSync(message: GenesisBlockMessage) {
        syncSession.clearTemporaryBlocks()

        val lastLocalGenesisBlock = blockManager.getLastGenesisBlock()
        val delegates = lastLocalGenesisBlock.getPayload().activeDelegates
        try {
            val currentGenesisBlock = GenesisBlock.of(message)

            if (lastLocalGenesisBlock.height <= currentGenesisBlock.height) {
                syncSession.init(nodeConfigurator.getConfig().mode, lastLocalGenesisBlock, currentGenesisBlock)
                requestEpoch(delegates)
            } else {
                checkBlock(lastLocalGenesisBlock)
            }
        } catch (e: Throwable) {
            log.error(e.message)
            syncFailed()
        }
    }

    private fun convertToBlocks(message: EpochResponseMessage): List<Block> {
        val genesisBlock = GenesisBlock.of(message.genesisBlock!!)
        val mainBlocks = message.mainBlocks.map { MainBlock.of(it) }

        return listOf(genesisBlock) + mainBlocks
    }

    private fun requestEpoch(delegates: List<String>) {
        val targetEpoch = if (syncSession.isEpochSynced()) {
            syncSession.getCurrentGenesisBlock().getPayload().epochIndex
        } else {
            (syncSession.minBlock as GenesisBlock).getPayload().epochIndex - 1
        }

        val message = EpochRequestMessage(targetEpoch, syncSession.syncMode)

        networkApiService.sendToAddress(message, getNodeInfos(delegates).shuffled().first())
        future = requestRetryScheduler.startRequestScheduler(future, Runnable { expired() })
    }

    private fun saveBlocks() {
        try {
            val epochHeight = consensusProperties.epochHeight!!
            val lastLocalBlock = blockManager.getLast()
            var indexFrom = lastLocalBlock.height + 1
            var indexTo = indexFrom + epochHeight
            var heights = (indexFrom..indexTo).toList()
            var temporaryBlocks = syncSession.getTemporaryBlocks(heights)
            while (!temporaryBlocks.isEmpty()) {
                temporaryBlocks.forEach { block ->
                    blockManager.add(block)
                    log.info("Blocks saved till ${block.height} from ${temporaryBlocks.last().height}")
                }
                indexFrom = indexTo + 1
                indexTo += epochHeight
                heights = (indexFrom..indexTo).toList()
                temporaryBlocks = syncSession.getTemporaryBlocks(heights)
            }
            syncSession.clear()
            status = SYNCHRONIZED
            log.info("Chain is $status")

        } catch (e: Throwable) {
            log.error("Save block is failed: $e")
            syncFailed()
        }
    }

    private fun clearUnconfirmedTransactions() {
        if (LIGHT == nodeConfigurator.getConfig().mode) {
            transactionManager.deleteUnconfirmedTransactions()
        }
    }

    private fun syncFailed() {
        syncSession.clear()
        status = NOT_SYNCHRONIZED
        log.error("Sync is FAILED")
    }

    private fun checkBlock(block: Block) {
        val delegate = epochService.getDelegatesPublicKeys().random()
        val nodeInfo = addressesHolder.getNodeInfoByUid(delegate)
        if (null != nodeInfo) {
            networkApiService.sendToAddress(BlockAvailabilityRequest(block.hash), nodeInfo)
        }
    }

    private fun expired() {
        val lastGenesisBlock = blockManager.getLastGenesisBlock()
        if (syncSession.getEpochAdded() == 0L) {
            checkBlock(lastGenesisBlock)
        } else {
            requestEpoch(lastGenesisBlock.getPayload().activeDelegates)
        }
    }

    private fun getNodeInfos(delegates: List<String>): List<NodeInfo> =
        delegates.mapNotNull { publicKey -> addressesHolder.getNodeInfoByUid(publicKey) }

}
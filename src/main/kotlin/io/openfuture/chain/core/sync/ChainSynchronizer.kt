package io.openfuture.chain.core.sync

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture

@Component
class ChainSynchronizer(
    private val blockService: BlockService,
    private val delegateService: DelegateService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val rewardTransactionService: RewardTransactionService,
    private val epochService: EpochService,
    private val scheduledSynchronizer: ScheduledSynchronizer
) {

    private var future: ScheduledFuture<*>? = null

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED

    private var syncSession: SyncSession? = null


    fun getStatus(): SyncStatus = status

    fun onGenesisBlockResponse(message: GenesisBlockMessage) {
        future?.cancel(true)
        val nodesInfo = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()
        try {
            val currentGenesisBlock = fromMessage(message)
            val lastLocalGenesisBlock = genesisBlockService.getLast()

            if (lastLocalGenesisBlock.height <= currentGenesisBlock.height) {
                syncSession = SyncSession(lastLocalGenesisBlock, currentGenesisBlock)
                requestEpoch(nodesInfo)
            } else {
                requestLatestGenesisBlock()
            }
        } catch (e: Throwable) {
            log.error(e.message)
            syncFailed()
        }
    }

    fun onEpochResponse(message: EpochResponseMessage) {
        future?.cancel(true)
        val nodesInfo = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()
        try {
            if (!message.isEpochExists) {
                requestEpoch(nodesInfo.filter { it.uid != message.nodeId })
                return
            }

            val genesisBlock = fromMessage(message.genesisBlock!!)
            val listBlocks: MutableList<Block> = mutableListOf(genesisBlock)

            listBlocks.addAll(message.mainBlocks.map {
                val mainBlock = MainBlock.of(it)
                mainBlock.payload.rewardTransaction = mutableListOf(RewardTransaction.of(it.rewardTransaction, mainBlock))
                mainBlock
            })

            if (!syncSession!!.add(listBlocks)) {
                requestEpoch(nodesInfo.filter { it.uid != message.nodeId })
                return
            }

            if (!syncSession!!.isCompleted()) {
                requestEpoch(nodesInfo)
                return
            }

            saveBlocks()
        } catch (e: Throwable) {
            log.error(e.message)
            syncFailed()
        }
    }

    fun isInSync(block: Block): Boolean {
        val lastBlock = blockService.getLast()
        return isValidHeight(block, lastBlock) && isValidPreviousHash(block, lastBlock)
    }


    @Synchronized
    fun checkLastBlock() {
        log.debug("Chain in status=$status")
        if (PROCESSING == status) {
            return
        }
        status = PROCESSING
        val block = blockService.getLast()
        checkBlock(block)
        future = scheduledSynchronizer.startRequestScheduler(future, Runnable { checkBlock(block) })
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        future?.cancel(true)
        if (-1L == response.height) {
            val invalidGenesisBlock = genesisBlockService.getLast()
            log.info("Rolling back epoch # ${invalidGenesisBlock.payload.epochIndex}")
            blockService.removeEpoch(invalidGenesisBlock)
            val lastGenesisBlock = genesisBlockService.getLast()
            val requestedBlock = if (1L == lastGenesisBlock.height) {
                blockService.getLast()
            } else {
                lastGenesisBlock
            }
            checkBlock(requestedBlock)
            future = scheduledSynchronizer.startRequestScheduler(future, Runnable { checkBlock(lastGenesisBlock) })
        } else {
            requestLatestGenesisBlock()
        }
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun requestLatestGenesisBlock() {
        val knownActiveDelegates = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()
        val message = SyncRequestMessage()

        networkApiService.sendToAddress(message, knownActiveDelegates.shuffled().first())
        future = scheduledSynchronizer.startRequestScheduler(future, Runnable { expired() })
    }

    private fun requestEpoch(listNodeInfo: List<NodeInfo>) {
        val targetEpoch = if (syncSession!!.isEpochSynced()) {
            syncSession!!.getCurrentGenesisBlock().payload.epochIndex
        } else {
            (syncSession!!.getStorage().last() as GenesisBlock).payload.epochIndex - 1
        }

        val message = EpochRequestMessage(targetEpoch)

        networkApiService.sendToAddress(message, listNodeInfo.shuffled().first())
        future = scheduledSynchronizer.startRequestScheduler(future, Runnable { expired() })
    }

    private fun getNodeInfo(delegate: Delegate): NodeInfo = NodeInfo(delegate.nodeId, NetworkAddress(delegate.host, delegate.port))

    private fun fromMessage(message: GenesisBlockMessage): GenesisBlock {
        val delegates = message.delegates.asSequence().map { delegateService.getByPublicKey(it) }.toMutableList()
        return GenesisBlock.of(message, delegates)
    }

    private fun saveBlocks() {
        try {
            val lastLocalBlock = blockService.getLast()
            val filteredStorage = syncSession!!.getStorage().filter { it.height > lastLocalBlock.height }

            filteredStorage.asReversed().forEach {
                if (it is MainBlock) {
                    val rewardTransaction = it.payload.rewardTransaction.first()
                    it.payload.rewardTransaction.clear()
                    blockService.save(it)
                    rewardTransaction.block = it
                    rewardTransactionService.save(rewardTransaction)
                } else {
                    blockService.save(it)
                }
            }

            syncSession = null
            status = SYNCHRONIZED
            log.debug("Chain is SYNCHRONIZED")

        } catch (e: Throwable) {
            log.error("Save block is failed: $e")
            syncFailed()
        }
    }

    private fun syncFailed() {
        syncSession = null
        status = NOT_SYNCHRONIZED
        log.debug("Sync is failed")
    }

    private fun checkBlock(block: Block) {
        val delegate = epochService.getDelegates().random().toNodeInfo()
        val message = BlockAvailabilityRequest(block.hash)
        networkApiService.sendToAddress(message, delegate)
    }

    private fun expired() {
        if (null == syncSession) {
            requestLatestGenesisBlock()
        } else {
            requestEpoch(genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList())
        }
    }

}
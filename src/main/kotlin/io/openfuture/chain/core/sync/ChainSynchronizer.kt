package io.openfuture.chain.core.sync

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.DBChecker
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.MainBlockMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceSchemaCreatedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import javax.xml.bind.ValidationException

@Component
class ChainSynchronizer(
    private val properties: NodeProperties,
    private val consensusProperties: ConsensusProperties,
    private val addressesHolder: AddressesHolder,
    private val blockManager: BlockManager,
    private val networkApiService: NetworkApiService,
    private val transactionManager: TransactionManager,
    private val epochService: EpochService,
    private val requestRetryScheduler: RequestRetryScheduler,
    private val dbChecker: DBChecker,
    private val nodeConfigurator: NodeConfigurator,
    private val syncSession: SyncSession
) : ApplicationListener<DataSourceSchemaCreatedEvent> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    private var future: ScheduledFuture<*>? = null

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED


    override fun onApplicationEvent(event: DataSourceSchemaCreatedEvent) {
        prepareDB(nodeConfigurator.getConfig().mode)
    }

    @EventListener
    fun eventSyncMode(syncMode: SyncMode) {
        nodeConfigurator.setMode(syncMode)
        prepareDB(syncMode)
    }

    fun prepareDB(syncMode: SyncMode) {
        status = SyncStatus.PROCESSING
        dbChecker.prepareDB(syncMode)
        status = SYNCHRONIZED
    }

    fun getStatus(): SyncStatus = status

    fun onEpochResponse(message: EpochResponseMessage) {
        future?.cancel(true)
        val delegates = blockManager.getLastGenesisBlock().getPayload().activeDelegates
        try {
            if (!message.isEpochExists) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if ((syncSession.syncMode == FULL) && !isValidEpoch(message.mainBlocks)) {
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
        } catch (e: Throwable) {
            log.error(e.message)
            syncFailed()
        }
    }

    fun isInSync(block: Block): Boolean {
        val lastBlock = blockManager.getLast()
        if (lastBlock.hash == block.hash) {
            return true
        }
        return isValidHeight(block, lastBlock) && isValidPreviousHash(block, lastBlock)
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
        val delegates = blockManager.getLastGenesisBlock().getPayload().activeDelegates
        try {
            val currentGenesisBlock = GenesisBlock.of(message)
            val lastLocalGenesisBlock = blockManager.getLastGenesisBlock()

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
        val listBlocks: MutableList<Block> = mutableListOf(GenesisBlock.of(message.genesisBlock!!))
        val mainBlocks = message.mainBlocks.map { MainBlock.of(it) }
        listBlocks.addAll(mainBlocks)
        return listBlocks
    }

    private fun isValidEpoch(mainBlocks: List<MainBlockMessage>): Boolean =
        isValidTransactionMerkleRoot(mainBlocks)
            && isValidStateMerkleRoot(mainBlocks)
            && isValidReceiptMerkleRoot(mainBlocks)
            && isValidTransactions(mainBlocks)

    private fun isValidRewardTransactions(list: List<RewardTransactionMessage>): Boolean =
        list.all { transactionManager.verify(RewardTransaction.of(it)) }

    private fun isValidVoteTransactions(list: List<VoteTransactionMessage>): Boolean =
        list.all { transactionManager.verify(VoteTransaction.of(it)) }

    private fun isValidDelegateTransactions(list: List<DelegateTransactionMessage>): Boolean =
        list.all { transactionManager.verify(DelegateTransaction.of(it)) }

    private fun isValidTransferTransactions(list: List<TransferTransactionMessage>): Boolean =
        list.all { transactionManager.verify(TransferTransaction.of(it)) }

    private fun isValidTransactions(blocks: List<MainBlockMessage>): Boolean {
        try {
            blocks.forEach { block ->
                if (!isValidRewardTransactions(block.rewardTransactions)) {
                    throw ValidationException("Invalid reward transaction in block: height #${block.height}, hash ${block.hash} ")
                }
                if (!isValidDelegateTransactions(block.delegateTransactions)) {
                    throw ValidationException("Invalid delegate transactions in block: height #${block.height}, hash ${block.hash}")
                }
                if (!isValidTransferTransactions(block.transferTransactions)) {
                    throw ValidationException("Invalid transfer transactions in block: height #${block.height}, hash ${block.hash}")
                }
                if (!isValidVoteTransactions(block.voteTransactions)) {
                    throw ValidationException("Invalid vote transactions in block: height #${block.height}, hash ${block.hash}")
                }
            }
        } catch (e: ValidationException) {
            log.warn("Transactions are invalid: ${e.message}")
            return false
        }

        return true
    }

    private fun isValidStateMerkleRoot(mainBlocks: List<MainBlockMessage>): Boolean {
        mainBlocks.forEach { block ->
            if (!isValidRootHash(block.stateMerkleHash, block.getAllStates().map { it.hash })) {
                log.warn("State merkle root is invalid in block: height #${block.height}, hash ${block.hash}")
                return false
            }
        }

        return true
    }

    private fun isValidReceiptMerkleRoot(mainBlocks: List<MainBlockMessage>): Boolean {
        mainBlocks.forEach { block ->
            if (!isValidRootHash(block.receiptMerkleHash, block.receipts.map { it.hash })) {
                log.warn("Receipt merkle root is invalid in block: height #${block.height}, hash ${block.hash}")
                return false
            }
        }

        return true
    }

    private fun isValidTransactionMerkleRoot(mainBlocks: List<MainBlockMessage>): Boolean {
        mainBlocks.forEach { block ->
            if (!isValidRootHash(block.transactionMerkleHash, block.getAllTransactions().map { it.hash })) {
                log.warn("Transaction merkle root is invalid in block: height #${block.height}, hash ${block.hash}")
                return false
            }
        }

        return true
    }

    private fun isValidRootHash(rootHash: String, hashes: List<String>): Boolean {
        if (hashes.isEmpty()) {
            return false
        }

        return rootHash == HashUtils.merkleRoot(hashes)
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

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
                    when (block) {
                        is MainBlock -> blockManager.add(block)
                        is GenesisBlock -> blockManager.add(block)
                    }
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
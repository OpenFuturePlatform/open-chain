package io.openfuture.chain.core.sync

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.entity.NetworkAddress
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
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import javax.xml.bind.ValidationException

@Component
class ChainSynchronizer(
    private val properties: NodeProperties,
    private val blockService: BlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val voteTransactionService: VoteTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val epochService: EpochService,
    private val requestRetryScheduler: RequestRetryScheduler,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    private var future: ScheduledFuture<*>? = null

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED

    private var syncSession: SyncSession? = null


    fun getStatus(): SyncStatus = status

    fun onEpochResponse(message: EpochResponseMessage) {
        future?.cancel(true)
        val nodesInfo = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()
        try {
            if (!message.isEpochExists) {
                requestEpoch(nodesInfo.filter { it.uid != message.nodeId })
                return
            }

            if (syncSession!!.syncMode == FULL &&
                !isValidMerkleRoot(message.mainBlocks) && !isValidTransactions(message.mainBlocks)) {
                requestEpoch(nodesInfo.filter { it.uid != message.nodeId })
                return
            }

            if (!syncSession!!.add(convertToBlocks(message))) {
                log.warn("Epoch #${message.genesisBlock!!.epochIndex} is invalid, requesting another node...")
                requestEpoch(nodesInfo.filter { it.uid != message.nodeId })
                return
            }

            val epochsFrom =  syncSession!!.getCurrentGenesisBlock().payload.epochIndex - syncSession!!.getLastLocalGenesisBlock().payload.epochIndex
            val epochsProcessed = syncSession!!.getCurrentGenesisBlock().payload.epochIndex - message.genesisBlock!!.epochIndex
            log.info("EpochResponseMessage: №$epochsProcessed FROM $epochsFrom is processed")
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
        val block = genesisBlockService.getLast()
        checkBlock(block)
        future = requestRetryScheduler.startRequestScheduler(future, Runnable { checkBlock(block) })
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        future?.cancel(true)
        if (-1L == response.height) {
            val invalidGenesisBlock = genesisBlockService.getLast()
            log.info("Rolling back epoch #${invalidGenesisBlock.payload.epochIndex}")
            blockService.removeEpoch(invalidGenesisBlock)
            val lastGenesisBlock = genesisBlockService.getLast()
            val requestedBlock = if (1L == lastGenesisBlock.height) {
                blockService.getLast()
            } else {
                lastGenesisBlock
            }
            checkBlock(requestedBlock)
            future = requestRetryScheduler.startRequestScheduler(future, Runnable { checkBlock(lastGenesisBlock) })
        } else {
            val lastGenesisBlock = response.genesisBlock ?: genesisBlockService.getLast().toMessage()
            initSync(lastGenesisBlock)
        }
    }

    private fun initSync(message: GenesisBlockMessage) {
        val nodesInfo = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()
        try {
            val currentGenesisBlock = GenesisBlock.of(message)
            val lastLocalGenesisBlock = genesisBlockService.getLast()

            if (lastLocalGenesisBlock.height <= currentGenesisBlock.height) {
                syncSession = SyncSession(properties.syncMode!!, lastLocalGenesisBlock, currentGenesisBlock)
                requestEpoch(nodesInfo)
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
        val mainBlocks = message.mainBlocks.map {
            val mainBlock = MainBlock.of(it)
            mainBlock.payload.rewardTransaction = mutableListOf(RewardTransaction.of(it.rewardTransaction, mainBlock))
            if (syncSession!!.syncMode == FULL) {
                it.voteTransactions.forEach { vTx -> mainBlock.payload.voteTransactions.add(VoteTransaction.of(vTx, mainBlock)) }
                it.delegateTransactions.forEach { dTx -> mainBlock.payload.delegateTransactions.add(DelegateTransaction.of(dTx, mainBlock)) }
                it.transferTransactions.forEach { vTx -> mainBlock.payload.transferTransactions.add(TransferTransaction.of(vTx, mainBlock)) }
            }
            mainBlock
        }
        listBlocks.addAll(mainBlocks)
        return listBlocks
    }

    private fun isValidRewardTransactions(message: RewardTransactionMessage): Boolean = rewardTransactionService.verify(message)

    private fun isValidVoteTransactions(list: List<VoteTransactionMessage>): Boolean = !list
        .any { !voteTransactionService.verify(it) }

    private fun isValidDelegateTransactions(list: List<DelegateTransactionMessage>): Boolean = !list
        .any { !delegateTransactionService.verify(it) }

    private fun isValidTransferTransactions(list: List<TransferTransactionMessage>): Boolean = !list
        .any { !transferTransactionService.verify(it) }

    private fun isValidTransactions(blocks: List<MainBlockMessage>): Boolean {
        try {
            for (block in blocks) {
                if (!isValidRewardTransactions(block.rewardTransaction)) {
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

    private fun isValidMerkleRoot(mainBlocks: List<MainBlockMessage>): Boolean {
        mainBlocks.forEach { block ->
            val hashes = mutableListOf<String>()
            hashes.addAll(block.transferTransactions.map { it.hash })
            hashes.addAll(block.voteTransactions.map { it.hash })
            hashes.addAll(block.delegateTransactions.map { it.hash })
            hashes.add(block.rewardTransaction.hash)
            if (block.merkleHash != MainBlockPayload.calculateMerkleRoot(hashes)) {
                log.warn("MerkleRoot is invalid in block: height #${block.height}, hash ${block.hash}")
                return false
            }
        }
        return true
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun requestEpoch(listNodeInfo: List<NodeInfo>) {
        val targetEpoch = if (syncSession!!.isEpochSynced()) {
            syncSession!!.getCurrentGenesisBlock().payload.epochIndex
        } else {
            (syncSession!!.getStorage().last() as GenesisBlock).payload.epochIndex - 1
        }

        val message = EpochRequestMessage(targetEpoch, syncSession!!.syncMode)

        networkApiService.sendToAddress(message, listNodeInfo.shuffled().first())
        future = requestRetryScheduler.startRequestScheduler(future, Runnable { expired() })
    }

    private fun getNodeInfo(delegate: Delegate): NodeInfo = NodeInfo(delegate.nodeId, NetworkAddress(delegate.host, delegate.port))

    private fun saveBlocks() {
        try {
            val lastLocalBlock = blockService.getLast()
            val filteredStorage = syncSession!!.getStorage().filter { it.height > lastLocalBlock.height }

            filteredStorage.asReversed().chunked(properties.syncBatchSize!!).forEach {
                blockService.saveChunk(it, syncSession!!.syncMode)
                log.debug("Blocks saved from ${it.first().height} to ${it.last().height}")
            }

            syncSession = null
            status = SYNCHRONIZED
            log.info("Chain is $status")

        } catch (e: Throwable) {
            log.error("Save block is failed: $e")
            syncFailed()
        }
    }

    private fun syncFailed() {
        syncSession = null
        status = NOT_SYNCHRONIZED
        log.error("Sync is FAILED")
    }

    private fun checkBlock(block: Block) {
        val delegate = epochService.getDelegates().random().toNodeInfo()
        val message = BlockAvailabilityRequest(block.hash)
        networkApiService.sendToAddress(message, delegate)
    }

    private fun expired() {
        val lastGenesisBlock = genesisBlockService.getLast()
        if (null == syncSession) {
            checkBlock(lastGenesisBlock)
        } else {
            requestEpoch(lastGenesisBlock.payload.activeDelegates.map { getNodeInfo(it) }.toList())
        }
    }

}
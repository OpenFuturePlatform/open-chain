package io.openfuture.chain.core.sync

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.sync.*
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.xml.bind.ValidationException

@Component
class ChainSynchronizer(
    private val properties: NodeProperties,
    private val addressesHolder: AddressesHolder,
    private val blockService: BlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val voteTransactionService: VoteTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val statePool: StatePool
) {

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED

    private var syncSession: SyncSession? = null


    fun getStatus(): SyncStatus = status

    @Synchronized
    fun sync() {
        log.debug("Chain in status=$status")
        if (PROCESSING == status) {
            return
        }
        status = PROCESSING

        requestLatestGenesisBlock()
    }

    fun onGenesisBlockResponse(message: GenesisBlockMessage) {
        resetRequestScheduler()
        val delegates = genesisBlockService.getLast().payload.activeDelegates
        try {
            val currentGenesisBlock = GenesisBlock.of(message)
            val lastLocalGenesisBlock = genesisBlockService.getLast()

            if (lastLocalGenesisBlock.height <= currentGenesisBlock.height) {
                syncSession = SyncSession(properties.syncMode!!, lastLocalGenesisBlock, currentGenesisBlock)
                requestEpoch(delegates)
            } else {
                requestLatestGenesisBlock()
            }
        } catch (e: Throwable) {
            log.error(e.message)
            syncFailed()
        }
    }

    fun onEpochResponse(message: EpochResponseMessage) {
        resetRequestScheduler()
        val delegates = genesisBlockService.getLast().payload.activeDelegates
        try {
            if (!message.isEpochExists) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if (syncSession!!.syncMode == FULL
                && !isValidTransactionMerkleRoot(message.mainBlocks)
                && !isValidStateMerkleRoot(message.mainBlocks)
                && !isValidTransactions(message.mainBlocks)
                && !isValidStates(message.mainBlocks)) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if (!syncSession!!.add(convertToBlocks(message))) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if (!syncSession!!.isCompleted()) {
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
        val lastBlock = blockService.getLast()
        return isValidHeight(block, lastBlock) && isValidPreviousHash(block, lastBlock)
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
            it.delegateStates.forEach { ds -> mainBlock.payload.delegateStates.add(DelegateState.of(ds, mainBlock)) }
            it.walletStates.forEach { ws -> mainBlock.payload.walletStates.add(WalletState.of(ws, mainBlock)) }
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
                    throw ValidationException("Invalid reward transaction")
                }
                if (!isValidDelegateTransactions(block.delegateTransactions)) {
                    throw ValidationException("Invalid delegate transactions")
                }
                if (!isValidTransferTransactions(block.transferTransactions)) {
                    throw ValidationException("Invalid transfer transactions")
                }
                if (!isValidVoteTransactions(block.voteTransactions)) {
                    throw ValidationException("Invalid vote transactions")
                }
            }
        } catch (e: ValidationException) {
            log.debug("Transactions are invalid, cause: ${e.message}")
            return false
        }
        return true
    }

    private fun isValidStates(blocks: List<MainBlockMessage>): Boolean {
        try {
            for (block in blocks) {
                if (!isValidStates(block.getAllTransactions(), block.getAllStates())) {
                    throw ValidationException("Invalid states")
                }
            }
        } catch (e: ValidationException) {
            log.debug("States are invalid, cause: ${e.message}")
            return false
        }
        return true
    }

    private fun isValidStates(txMessages: List<TransactionMessage>, blockStates: List<StateMessage>): Boolean {
        val states = getStates(txMessages)

        if (blockStates.size != states.size) {
            return false
        }

        return states.all { blockStates.contains(it) }
    }

    private fun getStates(txMessages: List<TransactionMessage>): List<StateMessage> {
        return statePool.use {
            txMessages.forEach { tx ->
                when (tx) {
                    is TransferTransactionMessage -> transferTransactionService.updateState(tx)
                    is VoteTransactionMessage -> voteTransactionService.updateState(tx)
                    is DelegateTransactionMessage -> delegateTransactionService.updateState(tx)
                    is RewardTransactionMessage -> rewardTransactionService.updateState(tx)
                }
            }

            statePool.getPool().values.toList()
        }
    }

    private fun isValidStateMerkleRoot(mainBlocks: List<MainBlockMessage>): Boolean {
        mainBlocks.forEach { block ->
            if (!isValidRootHash(block.stateHash, block.getAllStates().map { it.getHash() })) {
                log.debug("Invalid state hash: ${block.stateHash}")
                return false
            }
        }
        return true
    }

    private fun isValidTransactionMerkleRoot(mainBlocks: List<MainBlockMessage>): Boolean {
        mainBlocks.forEach { block ->
            if (!isValidRootHash(block.stateHash, block.getAllTransactions().map { it.hash })) {
                log.debug("Invalid transaction hash: ${block.stateHash}")
                return false
            }
        }
        return true
    }

    private fun isValidRootHash(rootHash: String, hashes: List<String>): Boolean {
        if (hashes.isEmpty()) {
            return false
        }

        return rootHash == MainBlockPayload.calculateMerkleRoot(hashes)
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun requestLatestGenesisBlock() {
        val message = SyncRequestMessage()
        val knownActiveDelegates = genesisBlockService.getLast().payload.activeDelegates

        networkApiService.sendToAddress(message, getNodeInfos(knownActiveDelegates).random())
        startRequestScheduler()
    }

    private fun requestEpoch(delegates: List<String>) {
        val targetEpoch = if (syncSession!!.isEpochSynced()) {
            syncSession!!.getCurrentGenesisBlock().payload.epochIndex
        } else {
            (syncSession!!.getStorage().last() as GenesisBlock).payload.epochIndex - 1
        }

        val message = EpochRequestMessage(targetEpoch, syncSession!!.syncMode)
        networkApiService.sendToAddress(message, getNodeInfos(delegates).shuffled().first())
        startRequestScheduler()
    }

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

    private fun startRequestScheduler() {
        if (future == null || future!!.isDone) {
            future = executor.scheduleWithFixedDelay(
                { expired() },
                properties.syncExpiry!!,
                properties.syncExpiry!!,
                TimeUnit.MILLISECONDS)
        }
    }

    private fun resetRequestScheduler() = future?.cancel(true)

    private fun expired() {
        if (null == syncSession) {
            requestLatestGenesisBlock()
        } else {
            requestEpoch(genesisBlockService.getLast().payload.activeDelegates)
        }
    }

    private fun getNodeInfos(delegates: List<String>): List<NodeInfo> =
        delegates.mapNotNull { publicKey -> addressesHolder.getNodeInfoByUid(publicKey) }

}
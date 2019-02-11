package io.openfuture.chain.core.sync

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.DBChecker
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
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
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import javax.xml.bind.ValidationException

@Component
class ChainSynchronizer(
    private val properties: NodeProperties,
    private val addressesHolder: AddressesHolder,
    private val blockService: BlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val transactionManager: TransactionManager,
    private val epochService: EpochService,
    private val requestRetryScheduler: RequestRetryScheduler,
    private val stateManager: StateManager,
    private val dbChecker: DBChecker,
    private val nodeKeyHolder: NodeKeyHolder
) : ApplicationListener<DataSourceSchemaCreatedEvent> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    private var syncSession: SyncSession? = null
    private var future: ScheduledFuture<*>? = null
    private var isBecomeDelegate = false

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED


    override fun onApplicationEvent(event: DataSourceSchemaCreatedEvent) {
        prepareDB()
    }

    fun prepareDB() {
        status = SyncStatus.PROCESSING
        dbChecker.prepareDB(getSyncMode())
        status = SYNCHRONIZED
    }

    fun getStatus(): SyncStatus = status

    fun onEpochResponse(message: EpochResponseMessage) {
        future?.cancel(true)
        val delegates = genesisBlockService.getLast().getPayload().activeDelegates
        try {
            if (!message.isEpochExists) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if ((isDelegate() || syncSession!!.syncMode == FULL) && !isValidEpoch(message.mainBlocks)) {
                requestEpoch(delegates.filter { it != message.delegateKey })
                return
            }

            if (!syncSession!!.add(convertToBlocks(message))) {
                log.warn("Epoch #${message.genesisBlock!!.epochIndex} is invalid, requesting another node...")
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
        val block = blockService.getLast()
        checkBlock(block)
        future = requestRetryScheduler.startRequestScheduler(future, Runnable { checkBlock(block) })
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        future?.cancel(true)
        if (-1L == response.height) {
            val invalidGenesisBlock = genesisBlockService.getLast()
            log.info("Rolling back epoch #${invalidGenesisBlock.getPayload().epochIndex}")
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

    fun isDelegate(): Boolean = stateManager.isExistsDelegateByPublicKey(nodeKeyHolder.getPublicKeyAsHexString())

    fun getSyncMode(): SyncMode {
        if (isBecomeDelegate || (LIGHT == properties.syncMode && isDelegate())) {
            isBecomeDelegate = true
            return FULL
        }
        return properties.syncMode!!
    }

    private fun initSync(message: GenesisBlockMessage) {
        val delegates = genesisBlockService.getLast().getPayload().activeDelegates
        try {
            val currentGenesisBlock = GenesisBlock.of(message)
            val lastLocalGenesisBlock = genesisBlockService.getLast()

            if (lastLocalGenesisBlock.height <= currentGenesisBlock.height) {
                syncSession = SyncSession(getSyncMode(), lastLocalGenesisBlock, currentGenesisBlock)
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

    private fun isValidRewardTransactions(message: RewardTransactionMessage): Boolean =
        transactionManager.verify(RewardTransaction.of(message))

    private fun isValidVoteTransactions(list: List<VoteTransactionMessage>): Boolean =
        list.all { transactionManager.verify(VoteTransaction.of(it)) }

    private fun isValidDelegateTransactions(list: List<DelegateTransactionMessage>): Boolean =
        list.all { transactionManager.verify(DelegateTransaction.of(it)) }

    private fun isValidTransferTransactions(list: List<TransferTransactionMessage>): Boolean =
        list.all { transactionManager.verify(TransferTransaction.of(it)) }

    private fun isValidTransactions(blocks: List<MainBlockMessage>): Boolean {
        try {
            blocks.forEach { block ->
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

        return rootHash == HashUtils.calculateMerkleRoot(hashes)
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun requestEpoch(delegates: List<String>) {
        val targetEpoch = if (syncSession!!.isEpochSynced()) {
            syncSession!!.getCurrentGenesisBlock().getPayload().epochIndex
        } else {
            (syncSession!!.getStorage().last() as GenesisBlock).getPayload().epochIndex - 1
        }

        val message = EpochRequestMessage(targetEpoch, syncSession!!.syncMode)

        networkApiService.sendToAddress(message, getNodeInfos(delegates).shuffled().first())
        future = requestRetryScheduler.startRequestScheduler(future, Runnable { expired() })
    }

    private fun saveBlocks() {
        try {
            val lastLocalBlock = blockService.getLast()
            val filteredStorage = syncSession!!.getStorage().filter { it.height > lastLocalBlock.height }

            filteredStorage.asReversed().chunked(properties.syncBatchSize!!).forEach {
                blockService.saveChunk(it, syncSession!!.syncMode)
                log.info("Blocks saved till ${it.last().height} from ${filteredStorage.first().height}")
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
        val delegate = epochService.getDelegatesPublicKeys().random()
        val nodeInfo = addressesHolder.getNodeInfoByUid(delegate)
        if (null != nodeInfo) {
            networkApiService.sendToAddress(BlockAvailabilityRequest(block.hash), nodeInfo)
        }
    }

    private fun expired() {
        val lastGenesisBlock = genesisBlockService.getLast()
        if (null == syncSession) {
            checkBlock(lastGenesisBlock)
        } else {
            requestEpoch(lastGenesisBlock.getPayload().activeDelegates)
        }
    }

    private fun getNodeInfos(delegates: List<String>): List<NodeInfo> =
        delegates.mapNotNull { publicKey -> addressesHolder.getNodeInfoByUid(publicKey) }

}
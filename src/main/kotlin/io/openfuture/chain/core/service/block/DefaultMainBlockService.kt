package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.BlockCapacityChecker
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.domain.block.TransactionSelectionRequest
import io.openfuture.chain.core.model.domain.block.TransactionSelectionResponse
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.NOT_SYNCHRONIZED
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.sync.MainBlockMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    blockService: BlockService,
    repository: MainBlockRepository,
    walletService: WalletService,
    delegateService: DelegateService,
    capacityChecker: BlockCapacityChecker,
    private val clock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val consensusProperties: ConsensusProperties,
    private val syncStatus: SyncStatus
) : BaseBlockService<MainBlock>(repository, blockService, walletService, delegateService, capacityChecker), MainBlockService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultMainBlockService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): MainBlock = repository.findOneByHash(hash)
        ?: throw NotFoundException("Block by $hash not found")

    @Transactional(readOnly = true)
    override fun getNextBlock(hash: String): MainBlock {
        val block = getByHash(hash)

        return repository.findFirstByHeightGreaterThan(block.height)
            ?: throw NotFoundException("Next block by hash $hash not found")
    }

    @Transactional(readOnly = true)
    override fun getPreviousBlock(hash: String): MainBlock {
        val block = getByHash(hash)

        return repository.findFirstByHeightLessThanOrderByHeightDesc(block.height)
            ?: throw NotFoundException("Previous block by hash $hash not found")
    }

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<MainBlock> = repository.findAll(request)

    @BlockchainSynchronized
    @Transactional(readOnly = true)
    override fun create(): PendingBlockMessage {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash

        val transactionsForBlock = getTransactions()
        val fees = transactionsForBlock.getAll().map { it.header.fee }.sum()
        val rewardTransactionMessage = rewardTransactionService.create(timestamp, fees)

        val merkleHash = calculateMerkleRoot(transactionsForBlock.getAll().map { it.footer.hash } + rewardTransactionMessage.hash)
        val payload = MainBlockPayload(merkleHash)

        val hash = createHash(timestamp, height, previousHash, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKeyAsHexString()

        return PendingBlockMessage(height, previousHash, timestamp, ByteUtils.toHexString(hash), signature, publicKey,
            merkleHash, rewardTransactionMessage, transactionsForBlock.voteTransactions.map { it.toMessage() },
            transactionsForBlock.delegateTransactions.map { it.toMessage() }, transactionsForBlock.transferTransactions.map { it.toMessage() })
    }

    @Transactional
    override fun add(message: PendingBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)

        if (!isSync(block)) {
            syncStatus.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        val savedBlock = super.save(block)
        rewardTransactionService.toBlock(message.rewardTransaction, savedBlock)
        message.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }
        message.delegateTransactions.forEach { delegateTransactionService.toBlock(it, savedBlock) }
        message.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
    }

    // todo need to improve!
    // todo this method is equal "override fun add(message: PendingBlockMessage)", this necessary because we can't to create PacketType with the same class inside
    @Transactional
    override fun add(message: MainBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)

        if (!isSync(block)) {
            syncStatus.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        val savedBlock = super.save(block)
        rewardTransactionService.toBlock(message.rewardTransaction, savedBlock)
        message.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }
        message.delegateTransactions.forEach { delegateTransactionService.toBlock(it, savedBlock) }
        message.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
    }

    @Transactional(readOnly = true)
    override fun verify(message: PendingBlockMessage): Boolean {
        return try {
            validate(message)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(message: PendingBlockMessage) {
        if (!isValidReward(message.getExternalTransactions().map { it.fee }.sum(), message.rewardTransaction.reward)) {
            throw ValidationException("Invalid reward: ${message.rewardTransaction.reward}")
        }

        if (!isValidMerkleHash(message.merkleHash, message.getAllTransactions().map { it.hash })) {
            throw ValidationException("Invalid merkle hash: ${message.merkleHash}")
        }

        if (!isValidVoteTransactions(message.voteTransactions)) {
            throw ValidationException("Invalid vote transactions")
        }

        if (!isValidDelegateTransactions(message.delegateTransactions)) {
            throw ValidationException("Invalid delegate transactions")
        }

        if (!isValidTransferTransactions(message.transferTransactions)) {
            throw ValidationException("Invalid transfer transactions")
        }

        super.validateBase(MainBlock.of(message))
    }

    private fun isValidReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletService.getActualBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
    }

    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
        return transactions.all { voteTransactionService.verify(it) } &&
            transactions.distinctBy { it.nodeId }.size == transactions.size
    }

    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
        return transactions.all { delegateTransactionService.verify(it) } &&
            transactions.distinctBy { it.delegateKey }.size == transactions.size
    }

    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean {
        val isValidBalances = transactions.groupBy { it.senderAddress }.entries.all {
            it.value.map { it.amount + it.fee }.sum() <= walletService.getBalanceByAddress(it.key)
        }

        return transactions.all { transferTransactionService.verify(it) } && isValidBalances
    }

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }
        return merkleHash == calculateMerkleRoot(transactions)
    }

    private fun calculateMerkleRoot(transactions: List<String>): String {
        if (transactions.size == 1) {
            return transactions.single()
        }
        var previousTreeLayout = transactions.sortedByDescending { it }.map { it.toByteArray() }
        var treeLayout = mutableListOf<ByteArray>()
        while (previousTreeLayout.size != 2) {
            for (i in 0 until previousTreeLayout.size step 2) {
                val leftHash = previousTreeLayout[i]
                val rightHash = if (i + 1 == previousTreeLayout.size) {
                    previousTreeLayout[i]
                } else {
                    previousTreeLayout[i + 1]
                }
                treeLayout.add(HashUtils.sha256(leftHash + rightHash))
            }
            previousTreeLayout = treeLayout
            treeLayout = mutableListOf()
        }
        return ByteUtils.toHexString(HashUtils.doubleSha256(previousTreeLayout[0] + previousTreeLayout[1]))
    }

    private fun getTransactions(): TransactionSelectionResponse {
        val request = createBlockTransactionsRequest()

        val voteTransactions =
            voteTransactionService.getAllUnconfirmed(PageRequest(0, request.voteTransactionsCount))

        val delegateTransactions =
            delegateTransactionService.getAllUnconfirmed(PageRequest(0, request.delegateTransactionsCount))

        val transferTransactions =
            transferTransactionService.getAllUnconfirmed(PageRequest(0, request.transferTransactionsCount))

        return TransactionSelectionResponse(voteTransactions, delegateTransactions, transferTransactions)
    }

    private fun createBlockTransactionsRequest(): TransactionSelectionRequest {
        val votesCountTotal = voteTransactionService.getUnconfirmedCount().toInt()
        val delegatesCountTotal = delegateTransactionService.getUnconfirmedCount().toInt()
        val transfersCountTotal = transferTransactionService.getUnconfirmedCount().toInt()

        if (votesCountTotal + delegatesCountTotal + transfersCountTotal <= consensusProperties.blockCapacity!!) {
            return TransactionSelectionRequest(votesCountTotal, delegatesCountTotal, transfersCountTotal)
        }

        if (transfersCountTotal <= consensusProperties.blockCapacity!! / 2) {
            return when {
                votesCountTotal <= (consensusProperties.blockCapacity!! - transfersCountTotal) / 2 -> {
                    val delegatesCount = consensusProperties.blockCapacity!! - (transfersCountTotal + votesCountTotal)
                    TransactionSelectionRequest(votesCountTotal, delegatesCount, transfersCountTotal)
                }
                delegatesCountTotal <= (consensusProperties.blockCapacity!! - transfersCountTotal) / 2 -> {
                    val votesCount = consensusProperties.blockCapacity!! - (transfersCountTotal + delegatesCountTotal)
                    TransactionSelectionRequest(votesCount, delegatesCountTotal, transfersCountTotal)
                }
                else -> {
                    val votesCount = (consensusProperties.blockCapacity!! - transfersCountTotal) / 2
                    val delegatesCount = (consensusProperties.blockCapacity!! - transfersCountTotal) / 2
                    TransactionSelectionRequest(votesCount, delegatesCount, transfersCountTotal)
                }
            }
        } else {
            return when {
                delegatesCountTotal + votesCountTotal <= consensusProperties.blockCapacity!! / 2 -> {
                    val transferCount = consensusProperties.blockCapacity!! - (votesCountTotal + delegatesCountTotal)
                    TransactionSelectionRequest(votesCountTotal, delegatesCountTotal, transferCount)
                }
                votesCountTotal <= consensusProperties.blockCapacity!! / 2 / 2 -> {
                    val delegatesCount = consensusProperties.blockCapacity!! / 2 - votesCountTotal
                    val transferCount = consensusProperties.blockCapacity!! - (votesCountTotal + delegatesCountTotal)
                    TransactionSelectionRequest(votesCountTotal, delegatesCount, transferCount)
                }
                delegatesCountTotal <= consensusProperties.blockCapacity!! / 2 / 2 -> {
                    val votesCount = consensusProperties.blockCapacity!! / 2 - delegatesCountTotal
                    val transferCount = consensusProperties.blockCapacity!! - (votesCountTotal + delegatesCountTotal)
                    TransactionSelectionRequest(votesCount, delegatesCountTotal, transferCount)
                }
                else -> {
                    val votesCount = consensusProperties.blockCapacity!! / 2 / 2
                    val delegatesCount = consensusProperties.blockCapacity!! / 2 / 2
                    val transferCount = consensusProperties.blockCapacity!! - (votesCountTotal + delegatesCountTotal)
                    TransactionSelectionRequest(votesCount, delegatesCount, transferCount)
                }
            }
        }
    }

}
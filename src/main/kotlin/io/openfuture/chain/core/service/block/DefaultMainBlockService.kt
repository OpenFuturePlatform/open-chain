package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.NOT_SYNCHRONIZED
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.max

@Service
class DefaultMainBlockService(
    blockService: BlockService,
    repository: MainBlockRepository,
    walletService: WalletService,
    delegateService: DelegateService,
    private val clock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val walletVoteService: WalletVoteService,
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val consensusProperties: ConsensusProperties,
    private val syncStatus: SyncStatus,
    private val throughput: TransactionThroughput
) : BaseBlockService<MainBlock>(repository, blockService, walletService, delegateService), MainBlockService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultMainBlockService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): MainBlock = repository.findOneByHash(hash)
        ?: throw NotFoundException("Block $hash not found")

    @Transactional(readOnly = true)
    override fun getNextBlock(hash: String): MainBlock = repository.findFirstByHeightGreaterThan(getByHash(hash).height)
        ?: throw NotFoundException("Block after $hash not found")

    @Transactional(readOnly = true)
    override fun getPreviousBlock(hash: String): MainBlock =
        repository.findFirstByHeightLessThanOrderByHeightDesc(getByHash(hash).height)
            ?: throw NotFoundException("Block before $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<MainBlock> = repository.findAll(request)

    @BlockchainSynchronized
    @Synchronized
    @Transactional(readOnly = true)
    override fun create(): PendingBlockMessage {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash

        val transactionsForBlock = getTransactions()
        val fees = transactionsForBlock.asSequence().map { it.header.fee }.sum()
        val rewardTransactionMessage = rewardTransactionService.create(timestamp, fees)

        val merkleHash = calculateMerkleRoot(transactionsForBlock.map { it.footer.hash } + rewardTransactionMessage.hash)
        val payload = MainBlockPayload(merkleHash)

        val hash = createHash(timestamp, height, previousHash, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKeyAsHexString()

        val voteTransactions = transactionsForBlock.asSequence()
            .filterIsInstance<UnconfirmedVoteTransaction>()
            .map { it.toMessage() }
            .toList()

        val delegateTransactions = transactionsForBlock.asSequence()
            .filterIsInstance<UnconfirmedDelegateTransaction>()
            .map { it.toMessage() }
            .toList()

        val transferTransactions = transactionsForBlock.asSequence()
            .filterIsInstance<UnconfirmedTransferTransaction>()
            .map { it.toMessage() }
            .toList()

        return PendingBlockMessage(height, previousHash, timestamp, ByteUtils.toHexString(hash), signature, publicKey,
            merkleHash, rewardTransactionMessage, voteTransactions, delegateTransactions, transferTransactions)
    }

    @Synchronized
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

    @Transactional
    @Synchronized
    override fun add(message: BaseMainBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        if (!isSync(block)) {
            syncStatus.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        val savedBlock = super.save(block)
        message.getAllTransactions().forEach {
            when (it) {
                is RewardTransactionMessage -> rewardTransactionService.toBlock(it, savedBlock)
                is TransferTransactionMessage -> transferTransactionService.toBlock(it, savedBlock)
                is DelegateTransactionMessage -> delegateTransactionService.toBlock(it, savedBlock)
                is VoteTransactionMessage -> voteTransactionService.toBlock(it, savedBlock)
            }
        }

        throughput.updateThroughput(message.getAllTransactions().size, savedBlock.height)
    }

    private fun validate(message: PendingBlockMessage) {
        super.validateBase(MainBlock.of(message))

        if (!isValidMerkleHash(message.merkleHash, message.getAllTransactions().map { it.hash })) {
            throw ValidationException("Invalid merkle hash: ${message.merkleHash}")
        }

        if (!isValidBalances(message.getExternalTransactions())) {
            throw ValidationException("Invalid balances")
        }

        if (!isValidRewardTransaction(message)) {
            throw ValidationException("Invalid reward transaction")
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
    }

    private fun isValidBalances(transactions: List<TransactionMessage>): Boolean =
        transactions.groupBy { it.senderAddress }.entries.all { sender ->
            sender.value.asSequence()
                .map {
                    when (it) {
                        is TransferTransactionMessage -> it.amount + it.fee
                        is DelegateTransactionMessage -> it.amount + it.fee
                        is VoteTransactionMessage -> it.fee
                        else -> 0
                    }
                }
                .sum() <= walletService.getBalanceByAddress(sender.key)
        }

    private fun isValidRewardTransaction(message: PendingBlockMessage): Boolean =
        isValidReward(message.getExternalTransactions().asSequence().map { it.fee }.sum(), message.rewardTransaction.reward) &&
            rewardTransactionService.verify(message.rewardTransaction)

    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
        if (transactions.isEmpty()) {
            return true
        }

        val validVotes = transactions.groupBy { it.senderAddress }.entries.all { sender ->
            val persistVotes = walletVoteService.getVotesByAddress(sender.key).map { it.id.nodeId }
            val pendingVotes = sender.value.asSequence().filter { VoteType.FOR.getId() == it.voteTypeId }.map { it.nodeId }.toList()

            val hasDuplicates = pendingVotes.intersect(persistVotes).isNotEmpty()
            !hasDuplicates && consensusProperties.delegatesCount!! >= persistVotes.size + pendingVotes.size
        }

        val existDelegates = delegateService.isExistsByNodeIds(transactions.map { it.nodeId })

        return transactions.all { voteTransactionService.verify(it) } && validVotes && existDelegates
    }

    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
        if (transactions.isEmpty()) {
            return true
        }

        return transactions.all { delegateTransactionService.verify(it) } &&
            !delegateService.isExistsByNodeIds(transactions.map { it.nodeId }) &&
            transactions.distinctBy { it.nodeId }.size == transactions.size
    }

    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean =
        transactions.all { transferTransactionService.verify(it) }

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }

        return merkleHash == calculateMerkleRoot(transactions)
    }

    private fun isValidReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletService.getActualBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
    }

    private fun calculateMerkleRoot(transactions: List<String>): String {
        if (transactions.size == 1) {
            return transactions.single()
        }

        var previousTreeLayout = transactions.asSequence().sortedByDescending { it }.map { it.toByteArray() }.toList()
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

    private fun getTransactions(): List<UnconfirmedTransaction> {
        var result = mutableListOf<UnconfirmedTransaction>()
        var capacity = consensusProperties.blockCapacity!!
        var counter: Int
        var trOffset = 0L
        var delOffset = 0L
        var vOffset = 0L

        do {
            counter = 0
            val trTx = transferTransactionService.getAllUnconfirmed(PageRequest(trOffset, max(capacity / 2, 1)))
            counter += trTx.size
            trOffset += trTx.size
            result.addAll(trTx)
            capacity -= trTx.size
            val delTx = delegateTransactionService.getAllUnconfirmed(PageRequest(delOffset, max(capacity / 2, 1)))
            counter += delTx.size
            delOffset += delTx.size
            result.addAll(delTx)
            capacity -= delTx.size
            val vTx = voteTransactionService.getAllUnconfirmed(PageRequest(vOffset, max(capacity / 2, 1)))
            counter += vTx.size
            vOffset += vTx.size
            result.addAll(vTx)
            capacity -= vTx.size

            result = filterTransactions(result)
            capacity = consensusProperties.blockCapacity!! - result.size
        } while (0 != counter && 0 < capacity)

        return result
    }

    private fun filterTransactions(transactions: List<UnconfirmedTransaction>): MutableList<UnconfirmedTransaction> {
        val result = mutableListOf<UnconfirmedTransaction>()
        val transactionsBySender = transactions.groupBy { it.header.senderAddress }

        transactionsBySender.forEach {
            var balance = walletService.getBalanceByAddress(it.key)
            val list = it.value.filter { tx ->
                balance -= when (tx) {
                    is UnconfirmedTransferTransaction -> tx.header.fee + tx.payload.amount
                    is UnconfirmedDelegateTransaction -> tx.header.fee + tx.payload.amount
                    is UnconfirmedVoteTransaction -> tx.header.fee
                    else -> 0
                }
                0 <= balance
            }
            result.addAll(list)
        }

        return result
    }

}
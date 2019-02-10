package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
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
    stateManager: StateManager,
    private val keyHolder: NodeKeyHolder,
    private val throughput: TransactionThroughput,
    private val statePool: StatePool,
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockRepository: GenesisBlockRepository,
    private val voteTransactionService: VoteTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val uTransferTransactionService: UTransferTransactionService,
    private val uVoteTransactionService: UVoteTransactionService,
    private val uDelegateTransactionService: UDelegateTransactionService,
    private val transactionValidatorManager: TransactionValidatorManager,
    private val receiptService: ReceiptService
) : BaseBlockService<MainBlock>(repository, blockService, stateManager), MainBlockService {

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
    @Transactional(readOnly = true)
    override fun create(): PendingBlockMessage {
        BlockchainLock.readLock.lock()
        try {
            val timestamp = System.currentTimeMillis()
            val lastBlock = blockService.getLast()
            val height = lastBlock.height + 1
            val previousHash = lastBlock.hash
            val publicKey = keyHolder.getPublicKeyAsHexString()
            val delegate = stateManager.getLastByAddress<DelegateState>(publicKey)

            var fees = 0L
            val transactionHashes = mutableListOf<String>()
            val transactionsForBlock = getTransactions()

            val voteTransactions = mutableListOf<VoteTransactionMessage>()
            val delegateTransactions = mutableListOf<DelegateTransactionMessage>()
            val transferTransactions = mutableListOf<TransferTransactionMessage>()

            transactionsForBlock.asSequence().forEach {
                fees += it.fee
                transactionHashes.add(it.hash)
                when (it) {
                    is UnconfirmedVoteTransaction -> voteTransactions.add(it.toMessage())
                    is UnconfirmedDelegateTransaction -> delegateTransactions.add(it.toMessage())
                    is UnconfirmedTransferTransaction -> transferTransactions.add(it.toMessage())
                }
            }

            val rewardTransactionMessage = rewardTransactionService.create(timestamp, fees).toMessage()
            val txMessages = listOf(
                *voteTransactions.toTypedArray(),
                *delegateTransactions.toTypedArray(),
                *transferTransactions.toTypedArray(),
                rewardTransactionMessage
            )

            val stateHashes = mutableListOf<String>()
            val receipts = processTransactions(txMessages, delegate.walletAddress)
            val states = statePool.getStates()
            val delegateStates = mutableListOf<DelegateStateMessage>()
            val accountStates = mutableListOf<AccountStateMessage>()

            states.asSequence().forEach {
                stateHashes.add(it.hash)
                when (it) {
                    is DelegateState -> delegateStates.add(it.toMessage())
                    is AccountState -> accountStates.add(it.toMessage())
                }
            }

            val transactionMerkleHash = HashUtils.calculateMerkleRoot(transactionHashes + rewardTransactionMessage.hash)
            val stateMerkleHash = HashUtils.calculateMerkleRoot(stateHashes)
            val receiptMerkleHash = HashUtils.calculateMerkleRoot(receipts.map { it.hash })
            val payload = MainBlockPayload(transactionMerkleHash, stateMerkleHash, receiptMerkleHash)
            val hash = blockService.createHash(timestamp, height, previousHash, payload)
            val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())

            return PendingBlockMessage(height, previousHash, timestamp, toHexString(hash), signature, publicKey,
                transactionMerkleHash, stateMerkleHash, receiptMerkleHash, rewardTransactionMessage, voteTransactions,
                delegateTransactions, transferTransactions, delegateStates, accountStates, receipts.map { it.toMessage() })
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional(readOnly = true)
    override fun verify(message: PendingBlockMessage): Boolean {
        BlockchainLock.readLock.lock()
        try {
            validate(message)
            return true
        } catch (ex: ValidationException) {
            log.warn("Block is invalid cause: ${ex.message}")
        } finally {
            BlockchainLock.readLock.unlock()
        }

        return false
    }

    @Transactional
    @Synchronized
    override fun add(message: BaseMainBlockMessage) {
        BlockchainLock.writeLock.lock()
        try {
            if (null != repository.findOneByHash(message.hash)) {
                return
            }

            val block = MainBlock.of(message)
            val savedBlock = super.save(block)

            message.getAllTransactions().forEach {
                val receipt = message.receipts.find { receipt -> receipt.transactionHash == it.hash }!!
                when (it) {
                    is RewardTransactionMessage -> rewardTransactionService.commit(RewardTransaction.of(it, savedBlock))
                    is TransferTransactionMessage ->
                        transferTransactionService.commit(TransferTransaction.of(it, savedBlock), Receipt.of(receipt, block))
                    is DelegateTransactionMessage ->
                        delegateTransactionService.commit(DelegateTransaction.of(it, savedBlock), Receipt.of(receipt, block))
                    is VoteTransactionMessage ->
                        voteTransactionService.commit(VoteTransaction.of(it, savedBlock), Receipt.of(receipt, block))
                    else -> throw IllegalStateException("Unsupported transaction type")
                }
            }

            message.getAllStates().forEach {
                val state = when (it) {
                    is DelegateStateMessage -> DelegateState.of(it, block)
                    is AccountStateMessage -> AccountState.of(it, block)
                    else -> throw IllegalStateException("Unsupported state type")
                }
                stateManager.commit(state)
            }

            message.receipts.forEach { receiptService.commit(Receipt.of(it, block)) }

            throughput.updateThroughput(message.getAllTransactions().size, savedBlock.height)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional(readOnly = true)
    override fun getBlocksByEpochIndex(epochIndex: Long): List<MainBlock> {
        val genesisBlock = genesisBlockRepository.findOneByPayloadEpochIndex(epochIndex) ?: return emptyList()
        val beginHeight = genesisBlock.height + 1
        val endEpochHeight = beginHeight + consensusProperties.epochHeight!! - 1
        val heights = (beginHeight..endEpochHeight).toList()
        return repository.findAllByHeightIn(heights)
    }

    private fun validate(message: PendingBlockMessage) {
        super.validateBase(MainBlock.of(message))

        if (!isValidRootHash(message.transactionMerkleHash, message.getAllTransactions().map { it.hash })) {
            throw ValidationException("Invalid transaction merkle hash in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidRootHash(message.stateMerkleHash, message.getAllStates().map { it.hash })) {
            throw ValidationException("Invalid state merkle hash in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidRootHash(message.receiptMerkleHash, message.receipts.map { it.hash })) {
            throw ValidationException("Invalid receipt merkle hash in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidBalances(message.getExternalTransactions())) {
            throw ValidationException("Invalid balances in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidRewardTransaction(message)) {
            throw ValidationException("Invalid reward transaction in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidVoteTransactions(message.voteTransactions)) {
            throw ValidationException("Invalid vote transactions in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidDelegateTransactions(message.delegateTransactions)) {
            throw ValidationException("Invalid delegate transactions in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidTransferTransactions(message.transferTransactions)) {
            throw ValidationException("Invalid transfer transactions in block: height #${message.height}, hash ${message.hash}")
        }

        if (!isValidReceiptsAndStates(message)) {
            throw ValidationException("Invalid block states and receipts")
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
                .sum() <= stateManager.getWalletBalanceByAddress(sender.key)
        }

    private fun isValidRewardTransaction(message: PendingBlockMessage): Boolean =
        isValidReward(message.getExternalTransactions().asSequence().map { it.fee }.sum(), message.rewardTransaction.reward) &&
            transactionValidatorManager.verify(RewardTransaction.of(message.rewardTransaction))

    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
        if (transactions.isEmpty()) {
            return true
        }

        val validVotes = transactions.groupBy { it.senderAddress }.entries.all { sender ->
            if (sender.value.size != 1) {
                return false
            }

            val accountState = stateManager.getLastByAddress<AccountState>(sender.key)
            val vote = sender.value.first()
            if (VoteType.values().none { it.getId() == vote.voteTypeId }) {
                throw ValidationException("Vote type with id: ${vote.voteTypeId} is not exists")
            }

            return when (VoteType.values().first { it.getId() == vote.voteTypeId }) {
                FOR -> null == accountState.voteFor
                AGAINST -> null != accountState.voteFor && vote.delegateKey == accountState.voteFor
            }
        }

        return validVotes && transactions.all { transactionValidatorManager.verify(VoteTransaction.of(it)) }
    }

    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
        if (transactions.isEmpty()) {
            return true
        }

        return transactions.all { transactionValidatorManager.verify(DelegateTransaction.of(it)) } &&
            !stateManager.isExistsDelegatesByPublicKeys(transactions.map { it.delegateKey }) &&
            transactions.distinctBy { it.delegateKey }.size == transactions.size
    }

    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean =
        transactions.all { transactionValidatorManager.verify(TransferTransaction.of(it)) }

    private fun isValidRootHash(rootHash: String, hashes: List<String>): Boolean {
        if (hashes.isEmpty()) {
            return false
        }

        return rootHash == HashUtils.calculateMerkleRoot(hashes)
    }

    private fun isValidReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = stateManager.getWalletBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
    }

    private fun isValidReceiptsAndStates(block: PendingBlockMessage): Boolean {
        val delegateWallet = stateManager.getLastByAddress<DelegateState>(block.publicKey).walletAddress
        val receipts = processTransactions(block.getAllTransactions(), delegateWallet).map { it.toMessage() }
        val states = statePool.getStates()

        if (block.receipts.size != receipts.size) {
            return false
        }

        if (block.getAllStates().size != states.size) {
            return false
        }

        return receipts.all { block.receipts.contains(it) } && states.all { block.getAllStates().contains(it.toMessage()) }
    }

    private fun processTransactions(txMessages: List<TransactionMessage>, delegateWallet: String): List<Receipt> {
        val receipts = mutableListOf<Receipt>()

        statePool.clear()

        txMessages.forEach { tx ->
            receipts.add(
                when (tx) {
                    is TransferTransactionMessage ->
                        transferTransactionService.process(UnconfirmedTransferTransaction.of(tx), delegateWallet)
                    is VoteTransactionMessage ->
                        voteTransactionService.process(UnconfirmedVoteTransaction.of(tx), delegateWallet)
                    is DelegateTransactionMessage ->
                        delegateTransactionService.process(UnconfirmedDelegateTransaction.of(tx), delegateWallet)
                    is RewardTransactionMessage ->
                        rewardTransactionService.process(RewardTransaction.of(tx))
                    else -> throw IllegalStateException("Unsupported transaction type")
                })
        }

        return receipts
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
            val trTx = uTransferTransactionService.getAll(PageRequest(trOffset, max(capacity / 2, 1)))
            counter += trTx.size
            trOffset += trTx.size
            result.addAll(trTx)
            capacity -= trTx.size
            val delTx = uDelegateTransactionService.getAll(PageRequest(delOffset, max(capacity / 2, 1)))
            counter += delTx.size
            delOffset += delTx.size
            result.addAll(delTx)
            capacity -= delTx.size
            val vTx = uVoteTransactionService.getAll(PageRequest(vOffset, max(capacity / 2, 1)))
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
        val transactionsBySender = transactions.groupBy { it.senderAddress }

        transactionsBySender.forEach {
            var balance = stateManager.getWalletBalanceByAddress(it.key)
            val list = it.value.filter { tx ->
                balance -= when (tx) {
                    is UnconfirmedTransferTransaction -> tx.fee + tx.getPayload().amount
                    is UnconfirmedDelegateTransaction -> tx.fee + tx.getPayload().amount
                    is UnconfirmedVoteTransaction -> tx.fee
                    else -> 0
                }
                0 <= balance
            }
            result.addAll(list)
        }

        return result
    }

}
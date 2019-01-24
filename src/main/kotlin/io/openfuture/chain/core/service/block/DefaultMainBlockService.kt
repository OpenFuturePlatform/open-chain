package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload.Companion.calculateMerkleRoot
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.WalletState
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
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.time.Clock
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
    delegateStateService: DelegateStateService,
    private val clock: Clock,
    private val keyHolder: NodeKeyHolder,
    private val throughput: TransactionThroughput,
    private val walletStateService: WalletStateService,
    private val statePool: StatePool,
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockRepository: GenesisBlockRepository,
    private val voteTransactionService: VoteTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService
) : BaseBlockService<MainBlock>(repository, blockService, delegateStateService), MainBlockService {

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
            val timestamp = clock.currentTimeMillis()
            val lastBlock = blockService.getLast()
            val height = lastBlock.height + 1
            val previousHash = lastBlock.hash

            var fees = 0L
            val transactionHashes = mutableListOf<String>()
            val transactionsForBlock = getTransactions()

            val voteTransactions = mutableListOf<VoteTransactionMessage>()
            val delegateTransactions = mutableListOf<DelegateTransactionMessage>()
            val transferTransactions = mutableListOf<TransferTransactionMessage>()

            transactionsForBlock.asSequence().forEach {
                fees += it.header.fee
                transactionHashes.add(it.footer.hash)
                when (it) {
                    is UnconfirmedVoteTransaction -> voteTransactions.add(it.toMessage())
                    is UnconfirmedDelegateTransaction -> delegateTransactions.add(it.toMessage())
                    is UnconfirmedTransferTransaction -> transferTransactions.add(it.toMessage())
                }
            }

            val rewardTransactionMessage = rewardTransactionService.create(timestamp, fees)
            val txMessages = listOf(
                *voteTransactions.toTypedArray(),
                *delegateTransactions.toTypedArray(),
                *transferTransactions.toTypedArray(),
                rewardTransactionMessage
            )

            val stateHashes = mutableListOf<String>()
            val states = getStates(txMessages)
            val delegateStates = mutableListOf<DelegateStateMessage>()
            val walletStates = mutableListOf<WalletStateMessage>()

            states.asSequence().forEach {
                stateHashes.add(it.getHash())
                when (it) {
                    is DelegateStateMessage -> delegateStates.add(it)
                    is WalletStateMessage -> walletStates.add(it)
                }
            }

            val merkleHash = calculateMerkleRoot(transactionHashes + rewardTransactionMessage.hash)
            val stateHash = calculateMerkleRoot(stateHashes)
            val payload = MainBlockPayload(merkleHash, stateHash)
            val hash = createHash(timestamp, height, previousHash, payload)
            val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
            val publicKey = keyHolder.getPublicKeyAsHexString()

            return PendingBlockMessage(height, previousHash, timestamp, toHexString(hash), signature, publicKey,
                merkleHash, stateHash, rewardTransactionMessage, voteTransactions, delegateTransactions,
                transferTransactions, delegateStates, walletStates)
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
            log.warn("Block is invalid: ${ex.message}")
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
                when (it) {
                    is RewardTransactionMessage -> rewardTransactionService.commit(RewardTransaction.of(it, savedBlock))
                    is TransferTransactionMessage -> transferTransactionService.commit(TransferTransaction.of(it, savedBlock))
                    is DelegateTransactionMessage -> delegateTransactionService.commit(DelegateTransaction.of(it, savedBlock))
                    is VoteTransactionMessage -> voteTransactionService.commit(VoteTransaction.of(it, savedBlock))
                    else -> throw IllegalStateException("The type doesn`t handle")
                }
            }

            message.getAllStates().forEach {
                when (it) {
                    is DelegateStateMessage -> delegateStateService.commit(DelegateState.of(it, block))
                    is WalletStateMessage -> walletStateService.commit(WalletState.of(it, block))
                    else -> throw IllegalStateException("The type doesn`t handle")
                }
            }

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
        return repository.findAllByHeightBetween(beginHeight, endEpochHeight)
    }

    private fun validate(message: PendingBlockMessage) {
        super.validateBase(MainBlock.of(message))

        if (!isValidRootHash(message.merkleHash, message.getAllTransactions().map { it.hash })) {
            throw ValidationException("Invalid merkle hash - ${message.merkleHash}")
        }

        if (!isValidRootHash(message.stateHash, message.getAllStates().map { it.getHash() })) {
            throw ValidationException("Invalid state hash - ${message.stateHash}")
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

        if (!isValidStates(message.getAllTransactions(), message.getAllStates())) {
            throw ValidationException("Invalid states")
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
                .sum() <= walletStateService.getBalanceByAddress(sender.key)
        }

    private fun isValidRewardTransaction(message: PendingBlockMessage): Boolean =
        isValidReward(message.getExternalTransactions().asSequence().map { it.fee }.sum(), message.rewardTransaction.reward) &&
            rewardTransactionService.verify(message.rewardTransaction)

    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
        if (transactions.isEmpty()) {
            return true
        }

        val validVotes = transactions.groupBy { it.senderAddress }.entries.all { sender ->
            if (sender.value.size != 1) {
                return false
            }

            val persistVote = walletStateService.getLastByAddress(sender.key)
            val vote = sender.value.first()
            return when (VoteType.getById(vote.voteTypeId)) {
                FOR -> null == persistVote?.voteFor
                AGAINST -> null != persistVote?.voteFor && vote.delegateKey == persistVote.voteFor
            }
        }

        return validVotes && transactions.all { voteTransactionService.verify(it) }
    }

    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
        if (transactions.isEmpty()) {
            return true
        }

        return transactions.all { delegateTransactionService.verify(it) } &&
            !delegateStateService.isExistsByPublicKeys(transactions.map { it.delegateKey }) &&
            transactions.distinctBy { it.delegateKey }.size == transactions.size
    }

    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean =
        transactions.all { transferTransactionService.verify(it) }

    private fun isValidRootHash(rootHash: String, hashes: List<String>): Boolean {
        if (hashes.isEmpty()) {
            return false
        }

        return rootHash == calculateMerkleRoot(hashes)
    }

    private fun isValidReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletStateService.getActualBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
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
            var balance = walletStateService.getBalanceByAddress(it.key)
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
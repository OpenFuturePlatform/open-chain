package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.BlockCapacityChecker
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.InsufficientTransactionsException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.NOT_SYNCHRONIZED
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
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
    private val consensusProperties: ConsensusProperties,
    private val syncManager: SyncManager
) : BaseBlockService<MainBlock>(repository, blockService, walletService, delegateService, capacityChecker), MainBlockService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultMainBlockService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): MainBlock = repository.findOneByHash(hash)
        ?: throw NotFoundException("Block by $hash not found")

    @Transactional(readOnly = true)
    override fun getNextBlock(hash: String): MainBlock {
        val block = getByHash(hash)

        return repository.findFirstByHeightGreaterThan(block.height) ?: throw NotFoundException("Next block not found")
    }

    @Transactional(readOnly = true)
    override fun getPreviousBlock(hash: String): MainBlock {
        val block = getByHash(hash)

        return repository.findFirstByHeightLessThanOrderByHeightDesc(block.height) ?: throw NotFoundException("Previous block not found")
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

        val voteTransactions = voteTransactionService.getAllUnconfirmed()
        val delegateTransactions = delegateTransactionService.getAllUnconfirmed()
        val transferTransactions = transferTransactionService.getAllUnconfirmed()
        val transactions = voteTransactions + delegateTransactions + transferTransactions

        val reward = transactions.map { it.header.fee }.sum() + consensusProperties.rewardBlock!!
        val merkleHash = calculateMerkleRoot(transactions.map { it.hash })
        val payload = MainBlockPayload(merkleHash)

        val hash = createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKey()

        return PendingBlockMessage(height, previousHash, timestamp, reward, ByteUtils.toHexString(hash), signature, publicKey,
            merkleHash, voteTransactions.map { it.toMessage() }, delegateTransactions.map { it.toMessage() }, transferTransactions.map { it.toMessage() })
    }

    @Transactional
    override fun add(message: PendingBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)

        if (!isSync(block)) {
            syncManager.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        val savedBlock = super.save(block)
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
            syncManager.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        val savedBlock = super.save(block)
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

    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
        return transactions.all { voteTransactionService.verify(it) }
    }

    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
        return transactions.all { delegateTransactionService.verify(it) }
    }

    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean {
        return transactions.all { transferTransactionService.verify(it) }
    }

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }
        return merkleHash == calculateMerkleRoot(transactions)
    }

    private fun calculateMerkleRoot(transactions: List<String>): String {
        if (transactions.isEmpty()) {
            throw InsufficientTransactionsException()
        }

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

}
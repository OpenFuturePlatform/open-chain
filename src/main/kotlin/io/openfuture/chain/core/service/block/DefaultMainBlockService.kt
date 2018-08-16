package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.BlockCapacityChecker
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.InsufficientTransactionsException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
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
    private val consensusProperties: ConsensusProperties
) : BaseBlockService<MainBlock>(repository, blockService, walletService, delegateService, capacityChecker), MainBlockService {

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): MainBlock = repository.findOneByHash(hash)
        ?: throw NotFoundException("Last block not found")

    @Transactional(readOnly = true)
    override fun getByPreviousHash(previousHash: String): MainBlock = repository.findOneByPreviousHash(previousHash)
        ?: throw NotFoundException("Last block not found")

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<MainBlock> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun create(): PendingBlockMessage {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash

        // -- transactions by type
        val voteTxs = voteTransactionService.getAllUnconfirmed()
        val delegateTxs = delegateTransactionService.getAllUnconfirmed()
        val transferTxs = transferTransactionService.getAllUnconfirmed()
        val transactions = voteTxs + delegateTxs + transferTxs

        val reward = transactions.map { it.fee }.sum() + consensusProperties.rewardBlock!!
        val merkleHash = calculateMerkleRoot(transactions.map { it.hash })
        val payload = MainBlockPayload(merkleHash)

        val hash = BlockUtils.createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKey()

        val block = MainBlock(timestamp, height, previousHash, reward, ByteUtils.toHexString(hash), signature, publicKey, payload)
        return PendingBlockMessage(block, voteTxs, delegateTxs, transferTxs)
    }

    @Transactional
    override fun add(message: PendingBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        if (!isValid(block, message.getAllTransactions())) {
            //TODO call second synchronization
            return
        }

        val savedBlock= super.save(block)
        message.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }
        message.delegateTransactions.forEach { delegateTransactionService.toBlock(it, savedBlock) }
        message.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
    }

    @Transactional
    override fun synchronize(message: MainBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        if (!isValid(block, message.getAllTransactions().map { it.hash })) {
            return
        }
        val savedBlock = super.save(block)
        message.voteTransactions.forEach { voteTransactionService.synchronize(it, savedBlock) }
        message.delegateTransactions.forEach { delegateTransactionService.synchronize(it, savedBlock) }
        message.transferTransactions.forEach { transferTransactionService.synchronize(it, savedBlock) }
    }

    @Transactional(readOnly = true)
    override fun isValid(message: PendingBlockMessage): Boolean = isValid(MainBlock.of(message), message.getAllTransactions())

    private fun isValid(block: MainBlock, transactions: List<String>): Boolean {
        return isValidMerkleHash(block.payload.merkleHash, transactions) && super.isValid(block)
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

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }
        return merkleHash == calculateMerkleRoot(transactions.map { it })
    }

}
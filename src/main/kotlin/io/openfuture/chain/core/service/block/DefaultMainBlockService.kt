package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.InsufficientTransactionsException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.*
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    blockService: BlockService,
    repository: MainBlockRepository,
    walletService: WalletService,
    delegateService: DelegateService,
    private val clock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val consensusProperties: ConsensusProperties,
    private val syncManager: SyncManager
) : BaseBlockService<MainBlock>(repository, blockService, walletService, delegateService), MainBlockService {

    @Transactional(readOnly = true)
    override fun create(): MainBlockMessage {
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

        val hash = BlockUtils.createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKey()

        return MainBlockMessage(height, previousHash, timestamp, reward, ByteUtils.toHexString(hash), signature, publicKey,
            merkleHash, voteTransactions.map { it.toMessage() }, delegateTransactions.map { it.toMessage() }, transferTransactions.map { it.toMessage() })
    }

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
    override fun isValid(message: MainBlockMessage): Boolean {
        return isValidMerkleHash(message.merkleHash, message.getAllTransactions().map { it.hash }) &&
            isValidVoteTransactions(message.voteTransactions) &&
            isValidDelegateTransactions(message.delegateTransactions) &&
            isValidTransferTransactions(message.transferTransactions) &&
            super.isValid(MainBlock.of(message))
    }

    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
        return transactions.all { voteTransactionService.isValid(it) }
    }

    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
        return transactions.all { delegateTransactionService.isValid(it) }
    }

    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean {
        return transactions.all { transferTransactionService.isValid(it) }
    }

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }
        return merkleHash == calculateMerkleRoot(transactions.map { it })
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
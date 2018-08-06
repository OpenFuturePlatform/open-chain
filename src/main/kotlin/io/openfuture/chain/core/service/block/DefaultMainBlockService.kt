package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.service.NetworkService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    blockService: BlockService,
    private val repository: BlockRepository<MainBlock>,
    private val clock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val transactionService: TransactionService,
    private val consensusProperties: ConsensusProperties,
    private val networkService: NetworkService
) : BaseBlockService(blockService), MainBlockService {

    @Transactional(readOnly = true)
    override fun create(): PendingBlockMessage {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val transactions = transactionService.getAllUnconfirmed()
        val previousHash = lastBlock.hash
        val reward = transactions.map { it.fee }.sum() + consensusProperties.rewardBlock!!
        val merkleHash = calculateMerkleRoot(transactions.map { it.hash })
        val payload = MainBlockPayload(merkleHash)

        val hash = BlockUtils.createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())

        val block = MainBlock(timestamp, height, previousHash, reward, ByteUtils.toHexString(hash), signature, publicKey, payload)
        return PendingBlockMessage(block, transactions)
    }

    @Transactional
    override fun add(message: PendingBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        if (!isValid(block, message.transactions)) {
            return
        }

        val savedBlock = repository.save(block)
        message.transactions
            .map { transactionService.getUnconfirmedByHash(it) }
            .forEach { transactionService.toBlock(it, repository.save(savedBlock)) }
        networkService.broadcast(message)
    }

    @Transactional
    override fun synchronize(message: MainBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        if (!isValid(block, message.transactions.map { it.hash })) {
            return
        }

        val savedBlock = repository.save(block)
        message.transactions.forEach { transactionService.synchronize(it, repository.save(savedBlock)) }
    }

    @Transactional(readOnly = true)
    override fun isValid(message: PendingBlockMessage): Boolean {
        val block = MainBlock.of(message)
        val transactions = message.transactions.map { transactionService.getUnconfirmedByHash(it) }
        return isValid(block, transactions.map { it.hash })
    }

    private fun isValid(block: MainBlock, transactions: List<String>): Boolean {
        return isValidMerkleHash(block.payload.merkleHash, transactions) && super.isValid(block)
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

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }
        return merkleHash == calculateMerkleRoot(transactions.map { it })
    }

}
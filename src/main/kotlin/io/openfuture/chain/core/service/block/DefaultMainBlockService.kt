package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.core.MainBlockMessage
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
    private val consensusProperties: ConsensusProperties
) : BaseBlockService(blockService), MainBlockService {

    @Transactional(readOnly = true)
    override fun create(): MainBlock {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val transactions = transactionService.getAllUnconfirmed()
        val previousHash = lastBlock.hash
        val reward = transactions.map { it.fee }.sum() + consensusProperties.rewardBlock!!
        val merkleHash = calculateMerkleRoot(transactions)
        val payload = MainBlockPayload(merkleHash)

        val hash = BlockUtils.createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())

        return MainBlock(timestamp, height, previousHash, reward, ByteUtils.toHexString(hash), signature, publicKey, payload)
    }

    @Transactional
    override fun add(message: MainBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        val transactions = message.transactions.map { transactionService.getUnconfirmedByHash(it) }

        if (!isValid(block, transactions)) {
            return
        }

        repository.save(block)
        // todo broadcast
    }

    @Transactional(readOnly = true)
    override fun isValid(message: MainBlockMessage): Boolean {
        val block = MainBlock.of(message)
        val transactions = message.transactions.map { transactionService.getUnconfirmedByHash(it) }
        return isValid(block, transactions)
    }

    private fun isValid(block: MainBlock, transactions: List<BaseTransaction>): Boolean {
        return isValidMerkleHash(block, transactions) && super.isValid(block)
    }

    private fun calculateMerkleRoot(transactions: List<BaseTransaction>): String {
        if (transactions.size == 1) {
            return transactions.single().hash
        }
        var previousTreeLayout = transactions.map { it.hash.toByteArray() }
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

    private fun isValidMerkleHash(block: MainBlock, transactions: List<BaseTransaction>): Boolean {
        if (transactions.isEmpty()) {
            return false
        }
        return block.payload.merkleHash == calculateMerkleRoot(transactions)
    }

}
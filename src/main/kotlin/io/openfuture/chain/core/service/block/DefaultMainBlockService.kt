package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
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
        val reward = transactions.map { it.payload.fee }.sum() + consensusProperties.rewardBlock!!
        val merkleHash = calculateMerkleRoot(transactions)

        val hash = BlockUtils.createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())

        return MainBlock(timestamp, height, hash, signature, publicKey, payload)
    }

    private fun createPayload(lastBlock: BaseBlock, transactions: List<BaseTransaction>): MainBlockPayload {

        return MainBlockPayload(previousHash, reward, merkleHash)
    }

    @Transactional
    override fun add(message: MainBlockMessage) {
        if (!isValid(message)) {
            return
        }

        val block = repository.findOneByHash(message.hash)
        if (null != block) {
            return
        }

        val persistBlock = repository.save(MainBlock.of(message))
        message.transactions.forEach { transactionService.toBlock(it, persistBlock) }
        // todo broadcast
    }

    @Transactional(readOnly = true)
    override fun isValid(message: MainBlockMessage): Boolean {
        val block = MainBlock.of(message)
        val transactions = message.transactions.map { transactionService.getUnconfirmedByHash(it) }
        return super.isValid(block)
            && !message.transactions.isEmpty()
            && isValidMerkleHash(transactions, message.merkleHash)
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

    private fun isValidMerkleHash(transactions: List<BaseTransaction>, merkleHash: String): Boolean {
        if (transactions.isEmpty()) {
            return false
        }

        return merkleHash == calculateMerkleRoot(transactions)
    }

}
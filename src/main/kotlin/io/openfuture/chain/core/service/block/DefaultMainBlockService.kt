package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    blockService: BlockService,
    private val repository: BlockRepository<MainBlock>,
    private val clock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val consensusProperties: ConsensusProperties
) : BaseBlockService(blockService), MainBlockService {

    @Transactional(readOnly = true)
    override fun create(): NetworkMainBlock {
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash!!
        val time = clock.networkTime()
        val transactions = getAllUnconfirmed()
        val reward = transactions.map { it.fee }.sum() + consensusProperties.rewardBlock!!
        val merkleHash = calculateMerkleRoot(transactions)

        return NetworkMainBlock(height, previousHash, time, reward, merkleHash, transactions)
            .sign(ByteUtils.toHexString(keyHolder.getPublicKey()), keyHolder.getPrivateKey())
    }

    @Transactional
    override fun add(dto: NetworkMainBlock) {
        if (!isValid(dto)) {
            return
        }

        val block = repository.findOneByHash(dto.hash!!)
        if (null != block) {
            return
        }

        val persistBlock = repository.save(MainBlock.of(dto))
        dto.transactions.forEach { toBlock(it, persistBlock) }
        // todo broadcast
    }

    @Transactional(readOnly = true)
    override fun isValid(block: NetworkMainBlock): Boolean {
        return super.isValid(block)
            && !block.transactions.isEmpty()
            && isValidMerkleHash(block.transactions, block.merkleHash)
    }

    private fun getAllUnconfirmed(): MutableSet<BaseTransactionDto> {
        val transactions = mutableSetOf<BaseTransactionDto>()
        transactions.addAll(voteTransactionService.getAllUnconfirmed().map { it.toMessage() })
        transactions.addAll(delegateTransactionService.getAllUnconfirmed().map { it.toMessage() })
        transactions.addAll(transferTransactionService.getAllUnconfirmed().map { it.toMessage() })
        return transactions
    }

    private fun calculateMerkleRoot(transactions: Set<BaseTransactionDto>): String {
        if (transactions.size == 1) {
            return transactions.single().hash
        }
        var previousTreeLayout = transactions.map { it.hash.toByteArray() }
        var treeLayout = mutableListOf<ByteArray>()
        while(previousTreeLayout.size != 2) {
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

    private fun isValidMerkleHash(transactions: MutableSet<BaseTransactionDto>, merkleHash: String): Boolean {
        if (transactions.isEmpty()) {
            return false
        }

        return merkleHash == calculateMerkleRoot(transactions)
    }

    private fun toBlock(dto: BaseTransactionDto, block: MainBlock) {
        when (dto) {
            is VoteTransactionDto -> voteTransactionService.toBlock(dto.hash, block)
            is TransferTransactionDto -> transferTransactionService.toBlock(dto.hash, block)
            is DelegateTransactionDto -> delegateTransactionService.toBlock(dto.hash, block)
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

}
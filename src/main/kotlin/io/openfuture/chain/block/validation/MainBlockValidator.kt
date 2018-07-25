package io.openfuture.chain.block.validation

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.BlockType
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.entity.transaction.base.BaseTransaction
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Component

@Component
class MainBlockValidator(
    private val properties: ConsensusProperties
) : BlockValidator {

    override fun isValid(block: Block): Boolean {
        val mainBlock = block as MainBlock
        val transactions = mainBlock.transactions

        if (transactions.isEmpty()) {
            return false
        }

        if (!verifyRewardTransaction(transactions, mainBlock.getPublicKey())) {
            return false
        }

        if (!verifyDuplicatedTransactions(transactions)) {
            return false
        }

        return block.merkleHash == BlockUtils.calculateMerkleRoot(transactions)
    }

    override fun getTypeId(): Int = BlockType.MAIN.id

    private fun verifyRewardTransaction(transactions: List<BaseTransaction>, senderPublicKey: String): Boolean {
        val rewardTransaction = transactions.first() as? RewardTransaction ?: return false

        if (rewardTransaction.senderPublicKey != senderPublicKey) {
            return false
        }

        if (properties.genesisAddress!! != rewardTransaction.senderAddress) {
            return false
        }

        if (transactions.stream().skip(1).anyMatch { it is RewardTransaction }) {
            return false
        }

        val fees = transactions.stream().skip(1).mapToLong { it.fee }.sum()
        return rewardTransaction.amount == (fees + properties.rewardBlock!!)
    }

    private fun verifyDuplicatedTransactions(transactions: List<BaseTransaction>): Boolean {
        val transactionHashes = transactions.map { it.hash }.toSet()
        return transactionHashes.size == transactions.size
    }

}
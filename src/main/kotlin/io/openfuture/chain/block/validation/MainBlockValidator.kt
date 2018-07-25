package io.openfuture.chain.block.validation

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.BlockType
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Component

@Component
class MainBlockValidator : BlockValidator {

    override fun isValid(block: Block): Boolean {
        val mainBlock = block as MainBlock
        val transactions = mainBlock.transactions

        if (transactions.isEmpty()) {
            return false
        }

        if (!transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = BlockUtils.calculateMerkleRoot(transactions)
        return block.merkleHash == transactionsMerkleHash
    }

    override fun getTypeId(): Int = BlockType.MAIN.id

    private fun transactionsIsWellFormed(transactions: List<Transaction>): Boolean {
        val transactionHashes = HashSet<String>()
        for (transaction in transactions) {
            val transactionHash = transaction.hash
            if (transactionHashes.contains(transactionHash)) {
                return false
            }

            transactionHashes.add(transactionHash)
        }

        return true
    }

}
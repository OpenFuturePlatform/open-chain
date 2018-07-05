package io.openfuture.chain.crypto.block

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.service.BlockService
import org.springframework.stereotype.Component

@Component
class BlockValidator(
        private val blockService: BlockService
) {

    fun isValid(block: Block): Boolean {
        val transactions = block.transactions

        if (transactions.isEmpty()) {
            return false
        }

        if (!transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = merkleTreeHashThatWillBeImplementedByGeorge(transactions)
        if (block.merkleHash != transactionsMerkleHash) {
            return false
        }

        return true
    }

    private fun transactionsIsWellFormed(transactions: Set<Transaction>): Boolean {
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

    private fun merkleTreeHashThatWillBeImplementedByGeorge(transactions: Set<Transaction>): String {
        return ""
    }

}
package io.openfuture.chain.crypto.block

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Component

@Component
class BlockValidator(
    private val blockService: BlockService
) {

    fun isValid(block: Block): Boolean {
        if (BlockUtils.calculateHash() != block.hash) {
            return false
        }

        val transactions = block.transactions

        if (transactions.isEmpty()) {
            return false
        }

        if (!transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = BlockUtils.calculateMerkleRoot(transactions)
        if (block.merkleHash != transactionsMerkleHash) {
            return false
        }

        val lastChainBlock = blockService.getLast()
        if (lastChainBlock != null) {
            val lastBlockHeight = lastChainBlock.height
            if (block.height != lastBlockHeight + 1) {
                return false
            }

            if (block.previousHash != lastChainBlock.hash) {
                return false
            }

            if (block.timestamp <= lastChainBlock.timestamp) {
                return false
            }
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

}
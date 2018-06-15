package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.transaction.TransactionRequest

/**
 * @author Homza Pavel
 */
data class BlockRequest(
        val version: Int,
        val nonce: Long,
        val timestamp: Long,
        var hash: String,
        val merkleHash: String,
        val previousHash: String,
        val nodeKey: String,
        val nodeSignature: String,
        val transactions: List<TransactionRequest>
)

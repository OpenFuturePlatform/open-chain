package io.openfuture.chain.domain.block

data class BlockRequest(
    val orderNumber: Int,
    val nonce: Long,
    val timestamp: Long,
    var hash: String,
    val merkleHash: String,
    val previousHash: String,
    val nodeKey: String,
    val nodeSignature: String,
    val transactions: List<TransactionRequest>
)

package io.openfuture.chain.rpc.domain.transaction.response

abstract class BaseTransactionResponse(
    val timestamp: Long,
    val fee: Long,
    val senderAddress: String,
    val senderSignature: String,
    val senderPublicKey: String,
    val hash: String,
    val blockHash: String?
)
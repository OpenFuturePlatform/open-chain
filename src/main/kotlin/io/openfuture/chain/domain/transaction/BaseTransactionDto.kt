package io.openfuture.chain.domain.transaction

abstract class BaseTransactionDto(
    val timestamp: Long,
    val amount: Double,
    val fee: Double,
    val recipientAddress: String,
    val senderKey: String,
    val senderAddress: String,
    val hash: String,
    val senderSignature: String
)

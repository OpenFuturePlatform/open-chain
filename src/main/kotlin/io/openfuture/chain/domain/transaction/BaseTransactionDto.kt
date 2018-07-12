package io.openfuture.chain.domain.transaction

abstract class BaseTransactionDto(
    val timestamp: Long,
    val amount: Double,
    val recipientKey: String,
    val recipientAddress: String,
    val senderKey: String,
    val senderAddress: String,
    val senderSignature: String,
    val hash: String
)

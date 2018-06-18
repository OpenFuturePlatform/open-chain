package io.openfuture.chain.domain.transaction

open class PendingTransactionRequest(
    val amount: Int,
    val timestamp: Long,
    val recipientKey: String,
    val senderKey: String,
    val signature: String
)
package io.openfuture.chain.domain.transaction

/**
 * @author Homza Pavel
 */
open class PendingTransactionRequest(
    val amount: Int,
    val timestamp: Long,
    val recipientkey: String,
    val senderKey: String,
    val signature: String
)
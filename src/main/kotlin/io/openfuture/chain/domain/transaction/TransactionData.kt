package io.openfuture.chain.domain.transaction

data class TransactionData(
        val amount: Long,
        val timestamp: Long,
        val recipientKey: String,
        val senderKey: String,
        val signature: String
)
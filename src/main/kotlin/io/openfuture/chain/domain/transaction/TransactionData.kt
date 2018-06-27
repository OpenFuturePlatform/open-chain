package io.openfuture.chain.domain.transaction


class TransactionData(
        val amount: Int,
        val timestamp: Long,
        val recipientKey: String,
        val senderKey: String,
        val signature: String
)
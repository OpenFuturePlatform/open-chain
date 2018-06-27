package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.Transaction

class TransactionDto(
        val amount: Int,
        val timestamp: Long,
        val recipientKey: String,
        val senderKey: String,
        val signature: String,
        var hash: String
) {

    constructor(transaction: Transaction) : this(
            transaction.amount,
            transaction.timestamp,
            transaction.recipientkey,
            transaction.senderKey,
            transaction.signature,
            transaction.hash
    )

}

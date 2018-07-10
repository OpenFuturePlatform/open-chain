package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.transaction.Transaction

open class TransactionDto(
        var timestamp: Long,
        var amount: Long,
        var recipientKey: String,
        var senderKey: String,
        var senderSignature: String,
        var hash: String
) {

    constructor(transaction: Transaction) : this(
            transaction.timestamp,
            transaction.amount,
            transaction.recipientKey,
            transaction.senderKey,
            transaction.senderSignature,
            transaction.hash
    )

}

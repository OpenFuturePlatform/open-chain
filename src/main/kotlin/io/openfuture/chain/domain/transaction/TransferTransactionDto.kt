package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.transaction.TransferTransaction

class TransferTransactionDto(
    timestamp: Long,
    amount: Long,
    recipientKey: String,
    senderKey: String,
    senderSignature: String,
    hash: String
): BaseTransactionDto(timestamp, amount, recipientKey, senderKey, senderSignature, hash) {

    constructor(transaction: TransferTransaction) : this(
        transaction.timestamp,
        transaction.amount,
        transaction.recipientKey,
        transaction.senderKey,
        transaction.senderSignature,
        transaction.hash
    )

}

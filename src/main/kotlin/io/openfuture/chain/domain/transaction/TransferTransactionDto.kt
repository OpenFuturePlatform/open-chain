package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.transaction.TransferTransaction

class TransferTransactionDto(
    timestamp: Long,
    amount: Long,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String
): BaseTransactionDto(timestamp, amount, recipientKey, recipientAddress, senderKey, senderAddress, senderSignature,
    hash) {

    constructor(transaction: TransferTransaction) : this(
        transaction.timestamp,
        transaction.amount,
        transaction.recipientKey,
        transaction.recipientAddress,
        transaction.senderKey,
        transaction.senderAddress,
        transaction.senderSignature,
        transaction.hash
    )

}

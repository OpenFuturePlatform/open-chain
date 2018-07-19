package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.transaction.TransferTransaction

class TransferTransactionDto(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto(timestamp, amount, fee, recipientAddress, senderKey, senderAddress, senderSignature, hash) {

    constructor(tx: TransferTransaction) : this(
        tx.timestamp,
        tx.amount,
        tx.fee,
        tx.recipientAddress,
        tx.senderKey,
        tx.senderAddress,
        tx.senderSignature,
        tx.hash
    )

}

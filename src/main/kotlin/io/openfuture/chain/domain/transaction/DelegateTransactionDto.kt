package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.DelegateTransaction

class DelegateTransactionDto(
    data: DelegateTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<DelegateTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: DelegateTransaction) : this(
        DelegateTransactionData(tx.amount, tx.recipientAddress, tx.senderAddress, tx.delegateKey),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

}
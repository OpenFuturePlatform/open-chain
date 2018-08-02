package io.openfuture.chain.core.model.dto.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction

class DelegateTransactionDto(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var delegateKey: String
) : BaseTransactionDto(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash) {

    constructor(tx: UDelegateTransaction) : this(
        tx.timestamp, tx.getPayload().fee, tx.senderAddress, tx.senderPublicKey, tx.senderSignature, tx.hash, tx.getPayload().delegateKey
    )

}
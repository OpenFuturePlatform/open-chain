package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction

class DelegateTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    val delegateKey: String
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey) {

    constructor(tx: UnconfirmedDelegateTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.delegateKey
    )

}
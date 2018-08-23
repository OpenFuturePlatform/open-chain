package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction

class DelegateTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    val delegateKey: String,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, blockHash) {

    constructor(tx: UnconfirmedDelegateTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.delegateKey
    )

    constructor(tx: DelegateTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.delegateKey,
        tx.block.hash
    )

}
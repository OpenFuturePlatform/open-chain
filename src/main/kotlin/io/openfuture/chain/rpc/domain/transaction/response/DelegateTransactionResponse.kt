package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction

class DelegateTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    hash: String,
    val delegateKey: String,
    val amount: Long? = null,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, hash, blockHash) {

    constructor(tx: UnconfirmedDelegateTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.signature,
        tx.publicKey,
        tx.hash,
        tx.getPayload().delegateKey,
        tx.getPayload().amount
    )

    constructor(tx: DelegateTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.signature,
        tx.publicKey,
        tx.hash,
        tx.getPayload().delegateKey,
        tx.getPayload().amount,
        tx.block?.hash
    )

}
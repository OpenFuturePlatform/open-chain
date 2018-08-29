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
    val nodeId: String,
    val amount: Long? = null,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, hash, blockHash) {

    constructor(tx: UnconfirmedDelegateTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.footer.senderSignature,
        tx.footer.senderPublicKey,
        tx.footer.hash,
        tx.payload.nodeId,
        tx.payload.amount
    )

    constructor(tx: DelegateTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.footer.senderSignature,
        tx.footer.senderPublicKey,
        tx.footer.hash,
        tx.payload.nodeId,
        tx.payload.amount,
        tx.block.hash
    )

}
package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction

class VoteTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    val voteTypeId: Int,
    val delegateKey: String
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey) {

    constructor(tx: UnconfirmedVoteTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.voteTypeId,
        tx.payload.delegateKey
    )

}
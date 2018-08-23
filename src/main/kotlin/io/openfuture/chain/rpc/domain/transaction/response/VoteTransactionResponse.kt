package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction

class VoteTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    hash: String,
    val voteTypeId: Int,
    val delegateKey: String,
    blockHash: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey, hash, blockHash) {

    constructor(tx: UnconfirmedVoteTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.hash,
        tx.payload.voteTypeId,
        tx.payload.delegateKey
    )

    constructor(tx: VoteTransaction) : this(
        tx.header.timestamp,
        tx.header.fee,
        tx.header.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.hash,
        tx.payload.voteTypeId,
        tx.payload.delegateKey,
        tx.block.hash
    )

}
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
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.signature,
        tx.publicKey,
        tx.hash,
        tx.getPayload().voteTypeId,
        tx.getPayload().delegateKey
    )

    constructor(tx: VoteTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.signature,
        tx.publicKey,
        tx.hash,
        tx.getPayload().voteTypeId,
        tx.getPayload().delegateKey,
        tx.block?.hash
    )

}
package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import javax.validation.constraints.NotBlank

class VoteTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
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
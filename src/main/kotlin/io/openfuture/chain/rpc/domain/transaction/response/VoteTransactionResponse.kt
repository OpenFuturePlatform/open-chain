package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import javax.validation.constraints.NotBlank

class VoteTransactionResponse(
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionResponse(senderAddress, senderPublicKey, senderSignature) {

    constructor(transaction: UVoteTransaction) : this(
        transaction.senderAddress,
        transaction.senderPublicKey,
        transaction.senderSignature,
        transaction.getPayload().fee,
        transaction.getPayload().voteTypeId,
        transaction.getPayload().delegateKey
    )

}
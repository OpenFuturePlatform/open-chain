package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.core.util.TransactionUtils
import javax.validation.constraints.NotBlank

class VoteTransactionRequest(
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionRequest(senderAddress, senderPublicKey, senderSignature) {

    override fun toUEntity(timestamp: Long): UVoteTransaction = UVoteTransaction(
        timestamp,
        senderAddress!!,
        senderPublicKey!!,
        senderSignature!!,
        TransactionUtils.createHash(VoteTransactionPayload(fee!!, voteTypeId!!, delegateKey!!),
            senderPublicKey!!, senderSignature!!),
        VoteTransactionPayload(fee!!, voteTypeId!!, delegateKey!!)
    )

}
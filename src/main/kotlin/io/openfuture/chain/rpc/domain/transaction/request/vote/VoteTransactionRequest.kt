package io.openfuture.chain.rpc.domain.transaction.request.vote

import io.openfuture.chain.rpc.domain.transaction.request.BaseTransactionRequest
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class VoteTransactionRequest(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    @field:NotNull var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionRequest(timestamp, fee, senderAddress, senderSignature, senderPublicKey)
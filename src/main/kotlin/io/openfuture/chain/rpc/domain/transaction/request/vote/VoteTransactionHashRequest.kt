package io.openfuture.chain.rpc.domain.transaction.request.vote

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class VoteTransactionHashRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotNull var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
)
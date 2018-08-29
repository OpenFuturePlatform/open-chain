package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.core.model.entity.dictionary.VoteType
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class VoteTransactionRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var hash: String? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotNull var voteType: VoteType? = null,
    @field:NotBlank var nodeId: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null
)
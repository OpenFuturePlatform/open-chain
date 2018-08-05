package io.openfuture.chain.rpc.domain.transaction.request.vote

import io.openfuture.chain.rpc.domain.transaction.request.BaseTransactionHashRequest
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class VoteTransactionHashRequest(
    timestamp: Long? = null,
    fee: Long? = null,
    @field:NotNull var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionHashRequest(timestamp, fee)
package io.openfuture.chain.rpc.domain.transaction

import javax.validation.constraints.NotBlank

class VoteTransactionRequest(
    @field:NotBlank var voteTypeId: Int? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionRequest()
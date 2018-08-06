package io.openfuture.chain.rpc.domain.transaction.request.delegate

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class DelegateTransactionHashRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var delegateKey: String? = null
)
package io.openfuture.chain.rpc.domain.transaction.request

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

abstract class BaseTransactionHashRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null
)
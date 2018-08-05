package io.openfuture.chain.rpc.domain.transaction.request

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

abstract class BaseTransactionRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null
)
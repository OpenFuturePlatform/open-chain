package io.openfuture.chain.rpc.domain.transaction

import javax.validation.constraints.NotBlank

abstract class BaseTransactionRequest(
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null
)
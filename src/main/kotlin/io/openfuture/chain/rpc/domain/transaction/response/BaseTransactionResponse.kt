package io.openfuture.chain.rpc.domain.transaction.response

import javax.validation.constraints.NotBlank

abstract class BaseTransactionResponse(
    @field:NotBlank var timestamp: Long? = null,
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null
)
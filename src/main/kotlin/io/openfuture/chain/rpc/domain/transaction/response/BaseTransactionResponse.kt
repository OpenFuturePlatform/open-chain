package io.openfuture.chain.rpc.domain.transaction.response

import javax.validation.constraints.NotBlank

abstract class BaseTransactionResponse(
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderPublicKey: String? = null,
    @field:NotBlank var senderSignature: String? = null
)
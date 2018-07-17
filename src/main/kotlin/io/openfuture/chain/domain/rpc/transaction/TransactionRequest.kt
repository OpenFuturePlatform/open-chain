package io.openfuture.chain.domain.rpc.transaction

import io.openfuture.chain.domain.rpc.base.BaseRequest
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

abstract class TransactionRequest(
    @field:NotNull var amount: Double? = null,
    @field:NotBlank var recipientKey: String? = null,
    @field:NotBlank var recipientAddress: String? = null,
    @field:NotBlank var senderKey: String? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderSignature: String? = null
) : BaseRequest()
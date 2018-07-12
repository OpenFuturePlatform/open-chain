package io.openfuture.chain.domain.transaction.data

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

abstract class BaseTransactionData(
    @field:NotNull var amount: Long? = null,
    @field:NotBlank var recipientKey: String? = null,
    @field:NotBlank var recipientAddress: String? = null,
    @field:NotBlank var senderKey: String? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderSignature: String? = null
) {

    abstract fun getByteData(): ByteArray

}
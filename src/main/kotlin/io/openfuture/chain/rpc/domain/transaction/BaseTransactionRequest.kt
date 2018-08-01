package io.openfuture.chain.rpc.domain.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import javax.validation.constraints.NotBlank

abstract class BaseTransactionRequest(
    @field:NotBlank var senderAddress: String? = null,
    @field:NotBlank var senderPublicKey: String? = null,
    @field:NotBlank var senderSignature: String? = null
) {

    abstract fun toUEntity(timestamp: Long) : UTransaction

}
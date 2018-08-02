package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import javax.validation.constraints.NotBlank

class DelegateTransactionResponse(
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionResponse(senderAddress, senderPublicKey, senderSignature) {

    constructor(transaction: UDelegateTransaction) : this(
        transaction.senderAddress,
        transaction.senderPublicKey,
        transaction.senderSignature,
        transaction.getPayload().fee,
        transaction.getPayload().delegateKey
    )

}
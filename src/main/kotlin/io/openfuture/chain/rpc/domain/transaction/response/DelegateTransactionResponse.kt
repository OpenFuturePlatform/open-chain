package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import javax.validation.constraints.NotBlank

class DelegateTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey) {

    constructor(tx: UDelegateTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.delegateKey
    )

}
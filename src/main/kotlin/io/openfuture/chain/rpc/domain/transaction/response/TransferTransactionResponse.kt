package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import javax.validation.constraints.NotBlank

class TransferTransactionResponse(
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
) : BaseTransactionResponse(senderAddress, senderPublicKey, senderSignature) {

    constructor(transaction: UTransferTransaction) : this(
        transaction.senderAddress,
        transaction.senderPublicKey,
        transaction.senderSignature,
        transaction.getPayload().fee,
        transaction.getPayload().amount,
        transaction.getPayload().recipientAddress
    )

}
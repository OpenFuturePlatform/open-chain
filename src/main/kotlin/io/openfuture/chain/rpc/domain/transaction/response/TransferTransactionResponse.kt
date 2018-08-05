package io.openfuture.chain.rpc.domain.transaction.response

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import javax.validation.constraints.NotBlank

class TransferTransactionResponse(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
) : BaseTransactionResponse(timestamp, fee, senderAddress, senderSignature, senderPublicKey) {

    constructor(tx: UTransferTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.amount,
        tx.payload.recipientAddress
    )

}
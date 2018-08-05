package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.util.TransactionUtils
import javax.validation.constraints.NotBlank

class TransferTransactionRequest(
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
) : BaseTransactionRequest(senderAddress, senderPublicKey, senderSignature) {

    override fun toUEntity(timestamp: Long): UTransferTransaction = UTransferTransaction(
        timestamp,
        senderAddress!!,
        senderPublicKey!!,
        senderSignature!!,
        TransactionUtils.createHash(TransferTransactionPayload(fee!!, amount!!, recipientAddress!!),
            senderPublicKey!!, senderSignature!!),
        TransferTransactionPayload(fee!!, amount!!, recipientAddress!!)
    )

}
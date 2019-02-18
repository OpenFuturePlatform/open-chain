package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UnconfirmedTransferTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private var payload: TransferTransactionPayload

) : UnconfirmedTransaction(timestamp, fee, senderAddress, hash, signature, publicKey) {

    companion object {
        fun of(message: TransferTransactionMessage): UnconfirmedTransferTransaction = UnconfirmedTransferTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            TransferTransactionPayload(message.amount, message.recipientAddress, message.data)
        )

        fun of(request: TransferTransactionRequest): UnconfirmedTransferTransaction = UnconfirmedTransferTransaction(
            request.timestamp!!, request.fee!!, request.senderAddress!!, request.hash!!, request.senderSignature!!,
            request.senderPublicKey!!, TransferTransactionPayload(request.amount!!, request.recipientAddress,
            request.data)
        )
    }


    fun getType(): TransferTransactionType = TransferTransactionType.getType(payload.recipientAddress, payload.data)

    override fun toMessage(): TransferTransactionMessage = TransferTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.amount, payload.recipientAddress,
        payload.data
    )

    override fun getPayload(): TransferTransactionPayload = payload

}
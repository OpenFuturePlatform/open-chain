package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UnconfirmedTransferTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,

    @Embedded
    var payload: TransferTransactionPayload

) : UnconfirmedTransaction(header, footer) {

    companion object {
        fun of(message: TransferTransactionMessage): UnconfirmedTransferTransaction = UnconfirmedTransferTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            TransferTransactionPayload(message.amount, message.recipientAddress)
        )

        fun of(request: TransferTransactionRequest): UnconfirmedTransferTransaction = UnconfirmedTransferTransaction(
            TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!),
            TransactionFooter(request.hash!!, request.senderSignature!!, request.senderPublicKey!!),
            TransferTransactionPayload(request.amount!!, request.recipientAddress!!)
        )
    }

    override fun toMessage(): TransferTransactionMessage = TransferTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        footer.hash,
        footer.senderSignature,
        footer.senderPublicKey,
        payload.amount,
        payload.recipientAddress
    )

}
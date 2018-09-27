package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,
    block: MainBlock,

    @Embedded
    val payload: TransferTransactionPayload

) : Transaction(header, footer, block) {

    companion object {
        fun of(message: TransferTransactionMessage, block: MainBlock): TransferTransaction = TransferTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            block,
            TransferTransactionPayload(message.amount, message.recipientAddress)
        )

        fun of(utx: UnconfirmedTransferTransaction, block: MainBlock): TransferTransaction = TransferTransaction(
            utx.header,
            utx.footer,
            block,
            utx.payload
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
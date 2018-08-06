package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: TransferTransactionPayload

) : Transaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(message: TransferTransactionMessage): TransferTransaction = TransferTransaction(
            message.timestamp,
            message.fee,
            message.senderAddress,
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            TransferTransactionPayload(message.amount, message.recipientAddress)
        )

        fun of(utx: UnconfirmedTransferTransaction): TransferTransaction = TransferTransaction(
            utx.timestamp,
            utx.fee,
            utx.senderAddress,
            utx.hash,
            utx.senderSignature,
            utx.senderPublicKey,
            utx.payload
        )
    }

    override fun getPayload(): TransactionPayload {
        return payload
    }

}
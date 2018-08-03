package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UTransferTransaction(
    timestamp: Long,
    payload: TransferTransactionPayload,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : UTransaction<TransferTransactionPayload>(timestamp, payload, senderAddress, senderPublicKey, senderSignature, hash) {

    companion object {
        fun of(dto: TransferTransactionMessage): UTransferTransaction = UTransferTransaction(
            dto.timestamp,
            TransferTransactionPayload(dto.fee, dto.amount, dto.recipientAddress),
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash
        )
    }

}
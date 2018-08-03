package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UTransferTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    override val payload: TransferTransactionPayload

) : UTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash, payload) {

    companion object {
        fun of(dto: TransferTransactionMessage): UTransferTransaction = UTransferTransaction(
            dto.timestamp,
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            TransferTransactionPayload(dto.fee, dto.amount, dto.recipientAddress)
        )
    }

}
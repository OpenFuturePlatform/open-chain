package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    override val payload: TransferTransactionPayload

) : Transaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash, payload) {

    companion object {
        fun of(utx: UTransferTransaction): TransferTransaction = TransferTransaction(
            utx.timestamp,
            utx.senderAddress,
            utx.senderPublicKey,
            utx.senderSignature,
            utx.hash,
            utx.payload
        )
    }

}
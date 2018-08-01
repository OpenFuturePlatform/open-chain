package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
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
    private var payload: TransferTransactionPayload

) : UTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash) {

    override fun toConfirmed(): TransferTransaction = TransferTransaction(
        timestamp,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload
    )

    override fun getPayload(): TransferTransactionPayload {
        return payload
    }

}
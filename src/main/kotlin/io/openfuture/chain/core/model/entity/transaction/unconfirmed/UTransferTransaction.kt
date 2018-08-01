package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UTransferTransaction(
    timestamp: Long,
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : UTransaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash) {

    override fun toConfirmed(): TransferTransaction = TransferTransaction(
        timestamp,
        amount,
        fee,
        recipientAddress,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash
    )

}
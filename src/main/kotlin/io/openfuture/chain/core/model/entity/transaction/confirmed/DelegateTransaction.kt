package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    override val payload: DelegateTransactionPayload

) : Transaction(timestamp, senderPublicKey, senderAddress, senderSignature, hash, payload) {

    companion object {
        fun of(utx: UDelegateTransaction): DelegateTransaction = DelegateTransaction(
            utx.timestamp,
            utx.senderAddress,
            utx.senderPublicKey,
            utx.senderSignature,
            utx.hash,
            utx.payload
        )
    }

}
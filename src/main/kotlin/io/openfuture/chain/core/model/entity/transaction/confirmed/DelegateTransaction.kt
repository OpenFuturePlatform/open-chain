package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: DelegateTransactionPayload

) : Transaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(message: DelegateTransactionMessage): DelegateTransaction = DelegateTransaction(
            message.timestamp,
            message.fee,
            message.senderAddress,
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            DelegateTransactionPayload(message.delegateKey)
        )

        fun of(utx: UnconfirmedDelegateTransaction): DelegateTransaction = DelegateTransaction(
            utx.timestamp,
            utx.fee,
            utx.senderAddress,
            utx.hash,
            utx.senderSignature,
            utx.senderPublicKey,
            utx.payload
        )
    }

    override fun getPayload(): TransactionPayload = payload

}
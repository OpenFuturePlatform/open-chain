package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
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
    signature: String,
    publicKey: String,

    @Embedded
    private val payload: DelegateTransactionPayload,

    block: MainBlock? = null
) : Transaction(timestamp, fee, senderAddress, hash, signature, publicKey, block) {

    companion object {
        fun of(message: DelegateTransactionMessage, block: MainBlock? = null): DelegateTransaction = DelegateTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            DelegateTransactionPayload(message.delegateKey, message.amount), block
        )

        fun of(utx: UnconfirmedDelegateTransaction, block: MainBlock? = null): DelegateTransaction = DelegateTransaction(
            utx.timestamp, utx.fee, utx.senderAddress, utx.hash, utx.signature, utx.publicKey, utx.getPayload(), block
        )
    }


    override fun toMessage(): DelegateTransactionMessage = DelegateTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.delegateKey, payload.amount
    )

    override fun getPayload(): DelegateTransactionPayload = payload

}
package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    header: TransactionHeader,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    block: MainBlock,

    @Embedded
    val payload: DelegateTransactionPayload

) : Transaction(header, hash, senderSignature, senderPublicKey, block) {

    companion object {
        fun of(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction = DelegateTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            block,
            DelegateTransactionPayload(message.delegateKey, message.delegateHost, message.delegatePort, message.amount)
        )

        fun of(utx: UnconfirmedDelegateTransaction, block: MainBlock): DelegateTransaction = DelegateTransaction(
            utx.header,
            utx.hash,
            utx.senderSignature,
            utx.senderPublicKey,
            block,
            utx.payload
        )
    }

    override fun getPayload(): TransactionPayload = payload

    override fun toMessage(): DelegateTransactionMessage = DelegateTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        hash,
        senderSignature,
        senderPublicKey,
        payload.delegateKey,
        payload.delegateHost,
        payload.delegatePort,
        payload.amount
    )

}
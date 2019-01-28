package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,
    block: MainBlock,

    @Embedded
    val payload: DelegateTransactionPayload

) : Transaction(header, footer, payload, block) {

    companion object {
        fun of(message: DelegateTransactionMessage, block: MainBlock): DelegateTransaction = DelegateTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            block,
            DelegateTransactionPayload(message.delegateKey, message.amount)
        )

        fun of(utx: UnconfirmedDelegateTransaction, block: MainBlock): DelegateTransaction = DelegateTransaction(
            utx.header,
            utx.footer,
            block,
            utx.payload
        )
    }


    override fun toMessage(): DelegateTransactionMessage = DelegateTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        footer.hash,
        footer.senderSignature,
        footer.senderPublicKey,
        payload.delegateKey,
        payload.amount
    )

}
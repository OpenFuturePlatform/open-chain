package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_delegate_transactions")
class UnconfirmedDelegateTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,

    @Embedded
    var payload: DelegateTransactionPayload

) : UnconfirmedTransaction(header, footer) {

    companion object {
        fun of(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            DelegateTransactionPayload(message.nodeId, message.delegateHost, message.delegatePort, message.amount)
        )

        fun of(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!),
            TransactionFooter(request.hash!!, request.senderSignature!!, request.senderPublicKey!!),
            DelegateTransactionPayload(request.nodeId!!, request.senderHost!!, request. senderPort!!, request.amount!!)
        )
    }

    override fun toMessage(): DelegateTransactionMessage = DelegateTransactionMessage (
        header.timestamp,
        header.fee,
        header.senderAddress,
        footer.hash,
        footer.senderSignature,
        footer.senderPublicKey,
        payload.nodeId,
        payload.delegateHost,
        payload.delegatePort,
        payload.amount
    )

}
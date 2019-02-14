package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_delegate_transactions")
class UnconfirmedDelegateTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private var payload: DelegateTransactionPayload

) : UnconfirmedTransaction(timestamp, fee, senderAddress, hash, signature, publicKey) {

    companion object {
        fun of(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            DelegateTransactionPayload(message.delegateKey, message.amount)
        )

        fun of(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            request.timestamp!!, request.fee!!, request.senderAddress!!, request.hash!!, request.senderSignature!!,
            request.senderPublicKey!!, DelegateTransactionPayload(request.delegateKey!!, request.amount!!)
        )
    }


    override fun toMessage(): DelegateTransactionMessage = DelegateTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.delegateKey, payload.amount
    )

    override fun getPayload(): DelegateTransactionPayload = payload

}
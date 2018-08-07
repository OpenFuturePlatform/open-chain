package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
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
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    var payload: DelegateTransactionPayload

) : UnconfirmedTransaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(dto: DelegateTransactionMessage): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            dto.timestamp,
            dto.fee,
            dto.hash,
            dto.senderAddress,
            dto.senderSignature,
            dto.senderPublicKey,
            DelegateTransactionPayload(dto.delegateKey)
        )

        fun of(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            request.timestamp!!,
            request.fee!!,
            request.senderAddress!!,
            TransactionUtils.generateHash(request.timestamp!!, request.fee!!, request.senderAddress!!,
                DelegateTransactionPayload(request.delegateKey!!)),
            request.senderSignature!!,
            request.senderPublicKey!!,
            DelegateTransactionPayload(request.delegateKey!!)
        )
    }

    override fun getPayload(): TransactionPayload {
        return payload
    }

}
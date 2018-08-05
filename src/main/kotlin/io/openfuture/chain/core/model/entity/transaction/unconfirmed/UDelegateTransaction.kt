package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_delegate_transactions")
class UDelegateTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    var payload: DelegateTransactionPayload

) : UTransaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(dto: DelegateTransactionMessage): UDelegateTransaction = UDelegateTransaction(
            dto.timestamp,
            dto.fee,
            dto.hash,
            dto.senderAddress,
            dto.senderSignature,
            dto.senderPublicKey,
            DelegateTransactionPayload(dto.delegateKey)
        )

        fun of(time: Long, request: DelegateTransactionRequest): UDelegateTransaction = UDelegateTransaction(
            time,
            request.fee!!,
            request.senderAddress!!,
            TransactionUtils.generateHash(time, request.fee!!, DelegateTransactionPayload(request.delegateKey!!)),
            request.senderSignature!!,
            request.senderPublicKey!!,
            DelegateTransactionPayload(request.delegateKey!!)
        )
    }

    override fun getPayload(): TransactionPayload {
        return payload
    }

}
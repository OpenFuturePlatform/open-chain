package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_delegate_transactions")
class UDelegateTransaction(
    timestamp: Long,
    payload: DelegateTransactionPayload,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String

) : UTransaction<DelegateTransactionPayload>(timestamp, payload, senderPublicKey, senderAddress, senderSignature, hash) {

    companion object {
        fun of(dto: DelegateTransactionMessage): UDelegateTransaction = UDelegateTransaction(
            dto.timestamp,
            DelegateTransactionPayload(dto.fee, dto.delegateKey),
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash
        )
    }

//    override fun getPayload(): DelegateTransactionPayload {
//        return payload
//    }

}
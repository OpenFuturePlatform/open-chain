package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_delegate_transactions")
class UDelegateTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    override val payload: DelegateTransactionPayload

) : UTransaction(timestamp, senderPublicKey, senderAddress, senderSignature, hash, payload) {

    companion object {
        fun of(dto: DelegateTransactionMessage): UDelegateTransaction = UDelegateTransaction(
            dto.timestamp,
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            DelegateTransactionPayload(dto.fee, dto.delegateKey)
        )
    }

}
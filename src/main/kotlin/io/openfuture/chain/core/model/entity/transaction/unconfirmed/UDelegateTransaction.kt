package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
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
    private var payload: DelegateTransactionPayload

) : UTransaction(timestamp, senderPublicKey, senderAddress, senderSignature, hash) {

    companion object {
        fun of(dto: DelegateTransactionDto): UDelegateTransaction = UDelegateTransaction(
            dto.timestamp,
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            DelegateTransactionPayload(dto.fee, dto.delegateKey)
        )
    }

    override fun toConfirmed(): DelegateTransaction = DelegateTransaction(
        timestamp,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload
    )

    override fun getPayload(): DelegateTransactionPayload {
        return payload
    }

}
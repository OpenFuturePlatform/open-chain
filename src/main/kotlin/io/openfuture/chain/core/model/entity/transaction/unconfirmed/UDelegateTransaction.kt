package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
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
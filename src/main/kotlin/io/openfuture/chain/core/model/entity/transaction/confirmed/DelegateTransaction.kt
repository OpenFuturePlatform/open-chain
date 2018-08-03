package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    timestamp: Long,
    payload: DelegateTransactionPayload,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String

) : Transaction<DelegateTransactionPayload>(timestamp, payload, senderPublicKey, senderAddress, senderSignature, hash)
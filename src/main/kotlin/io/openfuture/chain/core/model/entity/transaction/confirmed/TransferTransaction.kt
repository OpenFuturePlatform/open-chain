package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    payload: TransferTransactionPayload,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : Transaction<TransferTransactionPayload>(timestamp, payload, senderAddress, senderPublicKey, senderSignature, hash)
package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    payload: VoteTransactionPayload,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : Transaction<VoteTransactionPayload>(timestamp, payload, senderAddress, senderPublicKey, senderSignature, hash)
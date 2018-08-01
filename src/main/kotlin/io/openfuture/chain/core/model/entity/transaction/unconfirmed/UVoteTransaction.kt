package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UVoteTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    private var payload: VoteTransactionPayload

) : UTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash) {

    override fun toConfirmed(): VoteTransaction = VoteTransaction(
        timestamp,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload
    )

    override fun getPayload(): BaseTransactionPayload {
        return payload
    }

}
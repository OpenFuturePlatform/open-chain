package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    override val payload: VoteTransactionPayload

) : Transaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash, payload) {

    companion object {
        fun of(utx: UVoteTransaction): VoteTransaction = VoteTransaction(
            utx.timestamp,
            utx.senderAddress,
            utx.senderPublicKey,
            utx.senderSignature,
            utx.hash,
            utx.payload
        )
    }

//    override fun getPayload(): VoteTransactionPayload {
//        return payload
//    }

}
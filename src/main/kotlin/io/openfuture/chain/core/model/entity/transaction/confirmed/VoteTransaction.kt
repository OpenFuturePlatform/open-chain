package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    fee: Long,
    hash: String,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: VoteTransactionPayload

) : Transaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(utx: UVoteTransaction): VoteTransaction = VoteTransaction(
            utx.timestamp,
            utx.fee,
            utx.senderAddress,
            utx.hash,
            utx.senderSignature,
            utx.senderPublicKey,
            utx.payload
        )
    }

    override fun getPayload(): TransactionPayload {
        return payload
    }

}
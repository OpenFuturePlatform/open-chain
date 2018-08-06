package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.model.entity.transaction.vote.VoteTransactionPayload
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: VoteTransactionPayload

) : Transaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {

        fun of(message: VoteTransactionMessage): VoteTransaction = VoteTransaction(
            message.timestamp,
            message.fee,
            message.senderAddress,
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            VoteTransactionPayload(message.voteTypeId, message.delegateKey)
        )

        fun of(utx: UnconfirmedVoteTransaction): VoteTransaction = VoteTransaction(
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
package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.network.message.core.VoteTransactionMessage
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
    override val payload: VoteTransactionPayload

) : UTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash, payload) {

    companion object {
        fun of(dto: VoteTransactionMessage): UVoteTransaction = UVoteTransaction(
            dto.timestamp,
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            VoteTransactionPayload(dto.fee, dto.voteTypeId, dto.delegateKey)
        )
    }

}
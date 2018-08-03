package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UVoteTransaction(
    timestamp: Long,
    payload: VoteTransactionPayload,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String

) : UTransaction<VoteTransactionPayload>(timestamp, payload, senderAddress, senderPublicKey, senderSignature, hash) {

    companion object {
        fun of(dto: VoteTransactionMessage): UVoteTransaction = UVoteTransaction(
            dto.timestamp,
            VoteTransactionPayload(dto.fee, dto.voteTypeId, dto.delegateKey),
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash
        )
    }

}
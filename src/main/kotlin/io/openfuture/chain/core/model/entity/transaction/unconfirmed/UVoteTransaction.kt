package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
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

    companion object {
        fun of(dto: VoteTransactionDto): UVoteTransaction = UVoteTransaction(
            dto.timestamp,
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            VoteTransactionPayload(dto.fee, dto.voteTypeId, dto.delegateKey)
        )
    }

    override fun toConfirmed(): VoteTransaction = VoteTransaction(
        timestamp,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload
    )

    override fun getPayload(): VoteTransactionPayload {
        return payload
    }

}
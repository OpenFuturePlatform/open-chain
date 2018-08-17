package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.vote.VoteTransactionPayload
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UnconfirmedVoteTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: VoteTransactionPayload

) : UnconfirmedTransaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(dto: VoteTransactionMessage): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            dto.timestamp,
            dto.fee,
            dto.senderAddress,
            dto.hash,
            dto.senderSignature,
            dto.senderPublicKey,
            VoteTransactionPayload(dto.voteTypeId, dto.delegateKey)
        )

        fun of(request: VoteTransactionRequest): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            request.timestamp!!,
            request.fee!!,
            request.senderAddress!!,
            generateHash(
                request.timestamp!!,
                request.fee!!,
                request.senderAddress!!,
                VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!)),
            request.senderSignature!!,
            request.senderPublicKey!!,
            VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!)
        )
    }

    override fun getPayload(): TransactionPayload = payload

}
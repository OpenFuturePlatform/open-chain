package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UVoteTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: VoteTransactionPayload

) : UTransaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(dto: VoteTransactionMessage): UVoteTransaction = UVoteTransaction(
            dto.timestamp,
            dto.fee,
            dto.senderAddress,
            dto.hash,
            dto.senderSignature,
            dto.senderPublicKey,
            VoteTransactionPayload(dto.voteTypeId, dto.delegateKey)
        )

        fun of(time: Long, request: VoteTransactionRequest): UVoteTransaction = UVoteTransaction(
            time,
            request.fee!!,
            request.senderAddress!!,
            TransactionUtils.generateHash(time, request.fee!!, VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!)),
            request.senderSignature!!,
            request.senderPublicKey!!,
            VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!)
        )
    }

    override fun getPayload(): TransactionPayload {
        return payload
    }

}
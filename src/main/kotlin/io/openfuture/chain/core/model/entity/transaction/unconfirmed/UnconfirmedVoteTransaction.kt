package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
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
    signature: String,
    publicKey: String,

    @Embedded
    private val payload: VoteTransactionPayload

) : UnconfirmedTransaction(timestamp, fee, senderAddress, hash, signature, publicKey) {

    companion object {
        fun of(message: VoteTransactionMessage): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            VoteTransactionPayload(message.voteTypeId, message.delegateKey)
        )

        fun of(request: VoteTransactionRequest): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            request.timestamp!!, request.fee!!, request.senderAddress!!, request.hash!!, request.senderSignature!!,
            request.senderPublicKey!!, VoteTransactionPayload(request.voteTypeId!!, request.delegateKey!!)
        )
    }


    override fun toMessage(): VoteTransactionMessage = VoteTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.voteTypeId, payload.delegateKey
    )

    override fun getPayload(): VoteTransactionPayload = payload

}
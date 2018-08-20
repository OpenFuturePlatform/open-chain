package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.core.model.entity.transaction.vote.VoteTransactionPayload
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UnconfirmedVoteTransaction(
    header: TransactionHeader,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    val payload: VoteTransactionPayload

) : UnconfirmedTransaction(header, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(message: VoteTransactionMessage): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            VoteTransactionPayload(message.voteTypeId, message.delegateKey)
        )

        fun of(request: VoteTransactionRequest): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!),
            TransactionUtils.generateHash(
                TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!),
                VoteTransactionPayload(request.voteType!!.getId(), request.delegateKey!!)),
            request.senderSignature!!,
            request.senderPublicKey!!,
            VoteTransactionPayload(request.voteType!!.getId(), request.delegateKey!!)
        )
    }

    override fun getPayload(): TransactionPayload = payload

    override fun toMessage(): VoteTransactionMessage = VoteTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        hash,
        senderSignature,
        senderPublicKey,
        payload.voteTypeId,
        payload.delegateKey
    )

}
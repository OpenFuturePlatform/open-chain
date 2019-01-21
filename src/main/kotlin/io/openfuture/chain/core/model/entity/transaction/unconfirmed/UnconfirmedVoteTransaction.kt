package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UnconfirmedVoteTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,

    @Embedded
    val payload: VoteTransactionPayload

) : UnconfirmedTransaction(header, footer, payload) {

    companion object {
        fun of(message: VoteTransactionMessage): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            VoteTransactionPayload(message.voteTypeId, message.delegateKey)
        )

        fun of(request: VoteTransactionRequest): UnconfirmedVoteTransaction = UnconfirmedVoteTransaction(
            TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!),
            TransactionFooter(request.hash!!, request.senderSignature!!, request.senderPublicKey!!),
            VoteTransactionPayload(request.voteTypeId!!, request.nodeId!!)
        )
    }

    override fun toMessage(): VoteTransactionMessage = VoteTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        footer.hash,
        footer.senderSignature,
        footer.senderPublicKey,
        payload.voteTypeId,
        payload.delegateKey
    )

}
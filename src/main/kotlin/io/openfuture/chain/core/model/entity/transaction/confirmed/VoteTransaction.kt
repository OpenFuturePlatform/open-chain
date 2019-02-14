package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
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
    signature: String,
    publicKey: String,

    @Embedded
    private val payload: VoteTransactionPayload,

    block: MainBlock? = null
) : Transaction(timestamp, fee, senderAddress, hash, signature, publicKey, block) {

    companion object {
        fun of(message: VoteTransactionMessage, block: MainBlock? = null): VoteTransaction = VoteTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            VoteTransactionPayload(message.voteTypeId, message.delegateKey), block
        )

        fun of(utx: UnconfirmedVoteTransaction, block: MainBlock? = null): VoteTransaction = VoteTransaction(
            utx.timestamp, utx.fee, utx.senderAddress, utx.hash, utx.signature, utx.publicKey, utx.getPayload(), block
        )
    }


    override fun toMessage(): VoteTransactionMessage = VoteTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.voteTypeId, payload.delegateKey
    )

    override fun getPayload(): VoteTransactionPayload = payload

}
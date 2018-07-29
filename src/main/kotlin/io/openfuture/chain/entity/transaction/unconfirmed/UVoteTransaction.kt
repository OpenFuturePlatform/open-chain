package io.openfuture.chain.entity.transaction.unconfirmed

import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_vote_transactions")
class UVoteTransaction(
    timestamp: Long,
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Column(name = "vote_type_id", nullable = false)
    private var voteTypeId: Int,

    @Column(name = "delegate_key", nullable = false)
    var delegateKey: String

) : UTransaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash) {

    fun getVoteType(): VoteType = DictionaryUtils.valueOf(VoteType::class.java, voteTypeId)

    fun setVoteType(voteType: VoteType) {
        voteTypeId = voteType.getId()
    }

    override fun toConfirmed(): VoteTransaction = VoteTransaction(
        timestamp,
        amount,
        fee,
        recipientAddress,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        voteTypeId,
        delegateKey
    )

}
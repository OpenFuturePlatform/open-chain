package io.openfuture.chain.entity.transaction

import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    amount: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Column(name = "vote_type_id", nullable = false)
    private var voteTypeId: Int,

    @Column(name = "delegate_key", nullable = false)
    var delegateKey: String,

    block: MainBlock? = null

) : BaseTransaction(timestamp, amount, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash, block) {

    fun getVoteType() = DictionaryUtils.valueOf(VoteType::class.java, voteTypeId)

    fun setVoteType(voteType: VoteType) {
        voteTypeId = voteType.getId()
    }

}
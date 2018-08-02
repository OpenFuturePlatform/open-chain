package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.util.DictionaryUtil
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class VoteTransactionPayload(

    fee: Long,

    @Column(name = "vote_type_id", nullable = false)
    var voteTypeId: Int,

    @Column(name = "delegate_key", nullable = false)
    var delegateKey: String

) : BaseTransactionPayload(fee) {

    fun getVoteType(): VoteType {
        return DictionaryUtil.valueOf(VoteType::class.java, voteTypeId)
    }

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(fee)
        builder.append(voteTypeId)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}
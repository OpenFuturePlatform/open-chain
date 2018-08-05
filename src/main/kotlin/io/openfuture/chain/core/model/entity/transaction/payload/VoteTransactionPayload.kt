package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.util.ByteConstants
import io.openfuture.chain.core.util.DictionaryUtil
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class VoteTransactionPayload(

    @Column(name = "vote_type_id", nullable = false)
    var voteTypeId: Int,

    @Column(name = "delegate_key", nullable = false)
    var delegateKey: String

) : TransactionPayload {

    fun getVoteType(): VoteType {
        return DictionaryUtil.valueOf(VoteType::class.java, voteTypeId)
    }

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(ByteConstants.INT_BYTES + delegateKey.toByteArray(StandardCharsets.UTF_8).size)
        buffer.putInt(voteTypeId)
        buffer.put(delegateKey.toByteArray(StandardCharsets.UTF_8))
        return buffer.array()
    }

}
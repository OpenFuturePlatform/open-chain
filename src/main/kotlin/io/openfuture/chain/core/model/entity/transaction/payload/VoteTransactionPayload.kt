package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.util.ByteConstants
import io.openfuture.chain.core.util.DictionaryUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class VoteTransactionPayload(

    @Column(name = "vote_type_id", nullable = false)
    var voteTypeId: Int,

    @Column(name = "node_id", nullable = false)
    var nodeId: String

) : TransactionPayload {

    fun getVoteType(): VoteType = DictionaryUtils.valueOf(VoteType::class.java, voteTypeId)

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(ByteConstants.INT_BYTES + nodeId.toByteArray(StandardCharsets.UTF_8).size)
        buffer.putInt(voteTypeId)
        buffer.put(nodeId.toByteArray(StandardCharsets.UTF_8))
        return buffer.array()
    }

}
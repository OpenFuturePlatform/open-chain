package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class RewardTransactionPayload(

    @Column(name = "reward", nullable = false)
    var reward: Long,

    @Column(name = "recipient_address", nullable = false)
    var recipientAddress: String

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(LONG_BYTES + recipientAddress.toByteArray().size)
        buffer.putLong(reward)
        buffer.put(recipientAddress.toByteArray())
        return buffer.array()
    }

}
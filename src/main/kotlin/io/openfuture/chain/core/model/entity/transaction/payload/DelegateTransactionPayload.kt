package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.util.ByteConstants.INT_BYTES
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DelegateTransactionPayload(

    @Column(name = "node_id", nullable = false, unique = true)
    var nodeId: String,

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String,

    @Column(name = "delegate_host", nullable = false, unique = true)
    var delegateHost: String,

    @Column(name = "delegate_port", nullable = false, unique = true)
    var delegatePort: Int,

    @Column(name = "amount", nullable = false)
    var amount: Long

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(nodeId.toByteArray(UTF_8).size + delegateKey.toByteArray(UTF_8).size +
            delegateHost.toByteArray(UTF_8).size + INT_BYTES + LONG_BYTES)

        buffer.put(nodeId.toByteArray(UTF_8))
        buffer.put(delegateKey.toByteArray(UTF_8))
        buffer.put(delegateHost.toByteArray(UTF_8))
        buffer.putInt(delegatePort)
        buffer.putLong(amount)
        return buffer.array()
    }

}
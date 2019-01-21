package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.util.ByteConstants.INT_BYTES
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DelegateTransactionPayload(

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String,

    @Column(name = "amount", nullable = false)
    var amount: Long

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(delegateKey.toByteArray(UTF_8).size + INT_BYTES + LONG_BYTES)

        buffer.put(delegateKey.toByteArray(UTF_8))
        buffer.putLong(amount)
        return buffer.array()
    }

}
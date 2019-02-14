package io.openfuture.chain.core.model.entity.transaction.payload

import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Embeddable
import kotlin.Long.Companion.SIZE_BYTES

@Embeddable
class DelegateTransactionPayload(

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String,

    @Column(name = "amount", nullable = false)
    var amount: Long

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(delegateKey.toByteArray().size + SIZE_BYTES)
        buffer.put(delegateKey.toByteArray())
        buffer.putLong(amount)
        return buffer.array()
    }

}
package io.openfuture.chain.core.model.entity.transaction.payload

import org.apache.commons.lang3.StringUtils.EMPTY
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Embeddable
import kotlin.Long.Companion.SIZE_BYTES

@Embeddable
class TransferTransactionPayload(

    @Column(name = "amount", nullable = false)
    var amount: Long,

    @Column(name = "recipient_address")
    var recipientAddress: String? = null,

    @Column(name = "data")
    var data: String? = null

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val recipientAddressBytes = (recipientAddress ?: EMPTY).toByteArray()
        val dataBytes = (data ?: EMPTY).toByteArray()

        val buffer = ByteBuffer.allocate(SIZE_BYTES + recipientAddressBytes.size + dataBytes.size)
        buffer.putLong(amount)
        buffer.put(recipientAddressBytes)
        buffer.put(dataBytes)
        return buffer.array()
    }

}
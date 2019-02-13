package io.openfuture.chain.core.model.entity.transaction

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.io.Serializable
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TransactionHeader(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "fee", nullable = false)
    var fee: Long,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String

) : Serializable {

    @JsonIgnore
    fun getBytes(): ByteArray = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + senderAddress.toByteArray().size)
        .putLong(timestamp)
        .putLong(fee)
        .put(senderAddress.toByteArray())
        .array()

}
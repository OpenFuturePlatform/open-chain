package io.openfuture.chain.core.model.entity.transaction

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.persistence.Column
import javax.persistence.Embeddable
import kotlin.text.Charsets.UTF_8

@Embeddable
class TransactionHeader(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "fee", nullable = false)
    var fee: Long,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String

) {

    @JsonIgnore
    fun getBytes(): ByteArray = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + senderAddress.toByteArray(UTF_8).size)
        .putLong(timestamp)
        .putLong(fee)
        .put(senderAddress.toByteArray(StandardCharsets.UTF_8))
        .array()

}
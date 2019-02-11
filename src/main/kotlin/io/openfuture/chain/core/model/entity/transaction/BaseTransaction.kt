package io.openfuture.chain.core.model.entity.transaction

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.network.message.core.TransactionMessage
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import kotlin.Long.Companion.SIZE_BYTES

@MappedSuperclass
abstract class BaseTransaction(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "fee", nullable = false)
    var fee: Long,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @Column(name = "sender_key", nullable = false)
    var publicKey: String

) : BaseModel() {

    abstract fun toMessage(): TransactionMessage

    abstract fun getPayload(): TransactionPayload

    fun getBytes(): ByteArray = ByteBuffer.allocate(SIZE_BYTES + SIZE_BYTES +
        senderAddress.toByteArray().size + getPayload().getBytes().size)
        .putLong(timestamp)
        .putLong(fee)
        .put(senderAddress.toByteArray())
        .put(getPayload().getBytes())
        .array()

}
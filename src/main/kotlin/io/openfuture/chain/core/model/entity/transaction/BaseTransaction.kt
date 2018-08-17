package io.openfuture.chain.core.model.entity.transaction

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.util.ByteConstants
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.persistence.Column
import javax.persistence.MappedSuperclass

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

    @Column(name = "sender_signature", nullable = false)
    var senderSignature: String,

    @Column(name = "sender_key", nullable = false)
    var senderPublicKey: String

) : BaseModel() {

    companion object {

        fun generateHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload): String {
            val bytes = ByteBuffer.allocate(ByteConstants.LONG_BYTES + ByteConstants.LONG_BYTES +
                        senderAddress.toByteArray(StandardCharsets.UTF_8).size + payload.getBytes().size)
                .putLong(timestamp)
                .putLong(fee)
                .put(senderAddress.toByteArray(StandardCharsets.UTF_8))
                .put(payload.getBytes())
                .array()

            return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
        }

    }

    abstract fun getPayload(): TransactionPayload

}

package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.message.core.BlockMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import javax.persistence.*
import kotlin.Long.Companion.SIZE_BYTES

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Block(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @Column(name = "public_key", nullable = false)
    var publicKey: String

) : BaseModel() {

    companion object {
        fun generateHash(timestamp: Long, height: Long, previousHash: String, blockPayload: BlockPayload): String {
            val bytes = ByteBuffer.allocate(SIZE_BYTES + SIZE_BYTES +
                previousHash.toByteArray().size + blockPayload.getBytes().size)
                .putLong(timestamp)
                .putLong(height)
                .put(previousHash.toByteArray())
                .put(blockPayload.getBytes())
                .array()

            return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
        }
    }


    abstract fun toMessage(): BlockMessage

    abstract fun getPayload(): BlockPayload

    fun getBytes(): ByteArray = ByteBuffer.allocate(SIZE_BYTES + SIZE_BYTES +
        previousHash.toByteArray().size + getPayload().getBytes().size)
        .putLong(timestamp)
        .putLong(height)
        .put(previousHash.toByteArray())
        .put(getPayload().getBytes())
        .array()

}

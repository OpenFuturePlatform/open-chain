package io.openfuture.chain.core.model.entity

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.extension.*
import io.openfuture.chain.network.message.base.Message
import io.openfuture.chain.network.message.core.ReceiptMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import javax.persistence.*

@Entity
@Table(name = "receipts")
class Receipt(

    @Column(name = "transaction_hash", nullable = false)
    var transactionHash: String,

    @Column(name = "result", nullable = false)
    var result: String,

    @Column(name = "hash", nullable = false)
    var hash: String,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: Block? = null

) : BaseModel() {

    constructor(transactionHash: String, result: String) : this(
        transactionHash,
        result,
        lazy {
            val txHashBytes = transactionHash.toByteArray()
            val resultBytes = result.toByteArray()
            val bytes = ByteBuffer.allocate(txHashBytes.size + resultBytes.size)
                .put(txHashBytes)
                .put(resultBytes)
                .array()

            ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
        }.value
    )

    companion object {
        fun of(message: ReceiptMessage, block: MainBlock? = null): Receipt =
            Receipt(message.transactionHash, message.result, message.hash, block)

        fun generateResult(results: List<ReceiptResult>): String {
            val buffer = Unpooled.buffer()
            buffer.writeList(results)
            return ByteUtils.toHexString(buffer.array())
        }
    }

    fun getResults(): List<ReceiptResult> = Unpooled.copiedBuffer(ByteUtils.fromHexString(result)).readList()

    fun getBytes(): ByteArray {
        val txHashBytes = transactionHash.toByteArray()
        val resultBytes = result.toByteArray()

        return ByteBuffer.allocate(txHashBytes.size + resultBytes.size)
            .put(txHashBytes)
            .put(resultBytes)
            .array()
    }

    fun isSuccessful(): Boolean = getResults().all { null == it.error }

    fun toMessage(): ReceiptMessage = ReceiptMessage(transactionHash, result, hash)

}

@NoArgConstructor
class ReceiptResult(
    var from: String,
    var to: String,
    var amount: Long,
    var data: String? = null,
    var error: String? = null
) : Message {

    override fun read(buf: ByteBuf) {
        from = buf.readString()
        to = buf.readString()
        amount = buf.readLong()
        data = buf.readNullableString()
        error = buf.readNullableString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(from)
        buf.writeString(to)
        buf.writeLong(amount)
        buf.writeNullableString(data)
        buf.writeNullableString(error)
    }

}
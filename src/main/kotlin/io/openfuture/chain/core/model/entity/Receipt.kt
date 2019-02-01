package io.openfuture.chain.core.model.entity

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.extension.*
import io.openfuture.chain.network.message.core.ReceiptMessage
import io.openfuture.chain.network.serialization.Serializable
import org.apache.commons.lang3.StringUtils.EMPTY
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "receipts")
class Receipt(

    @Column(name = "transaction_hash", nullable = false)
    var transactionHash: String,

    @Column(name = "result", nullable = false)
    var result: String = EMPTY,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: Block? = null

) : BaseModel() {

    companion object {
        fun of(message: ReceiptMessage, block: MainBlock): Receipt =
            Receipt(message.transactionHash, message.result, block)
    }

    fun getResults(): List<ReceiptResult> = Unpooled.copiedBuffer(ByteUtils.fromHexString(result)).readList()

    fun setResults(results: List<ReceiptResult>) {
        val buffer = Unpooled.buffer()
        buffer.writeList(results)
        result = ByteUtils.toHexString(buffer.array())
    }

    fun toMessage(): ReceiptMessage = ReceiptMessage(transactionHash, result)

}

@NoArgConstructor
class ReceiptResult(
    var from: String,
    var to: String,
    var amount: Long,
    var data: String? = null,
    var error: String? = null
) : Serializable {

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
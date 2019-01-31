package io.openfuture.chain.core.model.entity

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.network.extension.*
import io.openfuture.chain.network.serialization.Serializable
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "receipts")
class Receipt(

    @Column(name = "transaction_hash", nullable = false)
    var transactionHash: String,

    @Column(name = "result", nullable = false)
    var result: String

) : BaseModel() {

    fun getResults(): List<ReceiptResult> = Unpooled.buffer().writeBytes(ByteUtils.fromHexString(result)).readList()

    fun setResults(results: List<ReceiptResult>) {
        val buffer = Unpooled.buffer()
        buffer.writeList(results)
        result = ByteUtils.toHexString(buffer.array())
    }

}

@NoArgConstructor
class ReceiptResult(
    var from: String,
    var to: String,
    var amount: Long,
    var data: String,
    var error: String?
) : Serializable {

    override fun read(buf: ByteBuf) {
        from = buf.readString()
        to = buf.readString()
        amount = buf.readLong()
        data = buf.readString()
        error = buf.readNullableString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(from)
        buf.writeString(to)
        buf.writeLong(amount)
        buf.writeString(data)
        buf.writeNullableString(error)
    }

}
package io.openfuture.chain.core.model.entity

import io.netty.buffer.Unpooled
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.network.extension.readNullableString
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeNullableString
import io.openfuture.chain.network.extension.writeString
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

) : BaseModel()

@NoArgConstructor
class ReceiptResult(
    var from: String,
    var to: String,
    var amount: Long,
    var data: String,
    var error: String?
) {

    fun toBytes(): ByteArray {
        val buffer = Unpooled.buffer()
        buffer.writeString(from)
        buffer.writeString(to)
        buffer.writeLong(amount)
        buffer.writeString(data)
        buffer.writeNullableString(error)
        return buffer.array()
    }

    fun fromBytes(bytes: ByteArray) {
        val buffer = Unpooled.buffer().writeBytes(bytes)
        from = buffer.readString()
        to = buffer.readString()
        amount = buffer.readLong()
        data = buffer.readString()
        error = buffer.readNullableString()
    }

}
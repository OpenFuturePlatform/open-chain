package io.openfuture.chain.network.domain.application.transaction.data

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.domain.base.BaseMessage

@NoArgConstructor
abstract class BaseTransactionData(
    var amount: Long,
    var fee: Long,
    var recipientAddress: String,
    var senderAddress: String
) : BaseMessage {

    abstract fun getBytes(): ByteArray

    override fun read(buffer: ByteBuf) {
        amount = buffer.readLong()
        fee = buffer.readLong()
        recipientAddress = buffer.readString()
        senderAddress = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(amount)
        buffer.writeLong(fee)
        buffer.writeString(recipientAddress)
        buffer.writeString(senderAddress)
    }

}
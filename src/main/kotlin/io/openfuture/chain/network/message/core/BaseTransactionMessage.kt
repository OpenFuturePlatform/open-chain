package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
abstract class BaseTransactionMessage(
    var timestamp: Long,
    var fee: Long,
    var senderAddress: String,
    var hash: String,
    var senderSignature: String,
    var senderPublicKey: String
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        timestamp = buffer.readLong()
        fee = buffer.readLong()
        senderAddress = buffer.readString()
        hash = buffer.readString()
        senderSignature = buffer.readString()
        senderPublicKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(timestamp)
        buffer.writeLong(fee)
        buffer.writeString(senderAddress)
        buffer.writeString(hash)
        buffer.writeString(senderSignature)
        buffer.writeString(senderPublicKey)
    }

}

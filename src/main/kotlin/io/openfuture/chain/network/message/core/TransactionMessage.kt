package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
abstract class TransactionMessage(
    var timestamp: Long,
    var fee: Long,
    var senderAddress: String,
    var hash: String,
    var senderSignature: String,
    var senderPublicKey: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        timestamp = buf.readLong()
        fee = buf.readLong()
        senderAddress = buf.readString()
        hash = buf.readString()
        senderSignature = buf.readString()
        senderPublicKey = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(timestamp)
        buf.writeLong(fee)
        buf.writeString(senderAddress)
        buf.writeString(hash)
        buf.writeString(senderSignature)
        buf.writeString(senderPublicKey)
    }

}

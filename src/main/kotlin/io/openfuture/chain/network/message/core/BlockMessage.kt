package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
abstract class BlockMessage(
    var height: Long,
    var previousHash: String,
    var timestamp: Long,
    var hash: String,
    var signature: String,
    var publicKey: String
) : Message {

    override fun read(buf: ByteBuf) {
        height = buf.readLong()
        previousHash = buf.readString()
        timestamp = buf.readLong()
        hash = buf.readString()
        signature = buf.readString()
        publicKey = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(height)
        buf.writeString(previousHash)
        buf.writeLong(timestamp)
        buf.writeString(hash)
        buf.writeString(signature)
        buf.writeString(publicKey)
    }

}
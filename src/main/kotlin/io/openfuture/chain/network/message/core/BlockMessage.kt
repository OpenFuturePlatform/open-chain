package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
abstract class BlockMessage(
    var height: Long,
    var previousHash: String,
    var timestamp: Long,
    var hash: String,
    var signature: String,
    var publicKey: String
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        height = buffer.readLong()
        previousHash = buffer.readString()
        timestamp = buffer.readLong()
        hash = buffer.readString()
        signature = buffer.readString()
        publicKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeString(previousHash)
        buffer.writeLong(timestamp)
        buffer.writeString(hash)
        buffer.writeString(signature)
        buffer.writeString(publicKey)
    }

}
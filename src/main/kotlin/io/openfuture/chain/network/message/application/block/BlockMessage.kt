package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
abstract class BlockMessage(
    var height: Long,
    var previousHash: String,
    var blockTimestamp: Long,
    var reward: Long,
    var publicKey: String,
    var hash: String,
    var signature: String
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        height = buffer.readLong()
        previousHash = buffer.readString()
        blockTimestamp = buffer.readLong()
        reward = buffer.readLong()
        publicKey = buffer.readString()
        hash = buffer.readString()
        signature = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeString(previousHash)
        buffer.writeLong(blockTimestamp)
        buffer.writeLong(reward)
        buffer.writeString(publicKey)
        buffer.writeString(hash)
        buffer.writeString(signature)
    }

}
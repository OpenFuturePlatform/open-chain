package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

abstract class NetworkBlock(
    var height: Long,
    var previousHash: String,
    var blockTimestamp: Long,
    var reward: Long,
    var publicKey: String,
    var hash: String,
    var signature: String
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        height = buffer.readLong()
        previousHash = buffer.readString()
        blockTimestamp = buffer.readLong()
        reward = buffer.readLong()
        publicKey = buffer.readString()
        hash = buffer.readString()
        signature = buffer.readString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeString(previousHash)
        buffer.writeLong(blockTimestamp)
        buffer.writeLong(reward)
        buffer.writeString(publicKey)
        buffer.writeString(hash)
        buffer.writeString(signature)
    }

}
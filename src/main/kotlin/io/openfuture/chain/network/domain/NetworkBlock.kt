package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
abstract class NetworkBlock(var height: Long,
                        var previousHash: String,
                        var merkleHash: String,
                        var blockTimestamp: Long,
                        var typeId: Int,
                        var hash: String,
                        var signature: String) : Packet() {


    override fun readParams(buffer: ByteBuf) {
        height = buffer.readLong()
        previousHash = buffer.readString()
        merkleHash = buffer.readString()
        blockTimestamp = buffer.readLong()
        typeId = buffer.readInt()
        hash = buffer.readString()
        signature = buffer.readString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeString(previousHash)
        buffer.writeString(merkleHash)
        buffer.writeLong(blockTimestamp)
        buffer.writeInt(typeId)
        buffer.writeString(hash)
        buffer.writeString(signature)
    }

}
package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

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
        previousHash = readString(buffer)
        merkleHash = readString(buffer)
        blockTimestamp = buffer.readLong()
        typeId = buffer.readInt()
        hash = readString(buffer)
        signature = readString(buffer)
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeLong(height)
        writeString(buffer, previousHash)
        writeString(buffer, merkleHash)
        buffer.writeLong(blockTimestamp)
        buffer.writeInt(typeId)
        writeString(buffer, hash)
        writeString(buffer, signature)
    }

}
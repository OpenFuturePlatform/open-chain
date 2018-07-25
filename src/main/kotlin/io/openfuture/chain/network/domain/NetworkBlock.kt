package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import java.nio.charset.StandardCharsets.UTF_8

@NoArgConstructor
abstract class NetworkBlock(var height: Long = 0,
                        var previousHash: String,
                        var merkleHash: String,
                        var blockTimestamp: Long = 0,
                        var typeId: Int = 0,
                        var hash: String,
                        var signature: String) : Packet() {


    override fun readParams(buffer: ByteBuf) {
        height = buffer.readLong()
        previousHash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        merkleHash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        blockTimestamp = buffer.readLong()
        typeId = buffer.readInt()
        hash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        signature = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeInt(previousHash.length)
        buffer.writeCharSequence(previousHash, UTF_8)
        buffer.writeInt(merkleHash.length)
        buffer.writeCharSequence(merkleHash, UTF_8)
        buffer.writeLong(blockTimestamp)
        buffer.writeInt(typeId)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)
        buffer.writeInt(signature.length)
        buffer.writeCharSequence(signature, UTF_8)
    }

}
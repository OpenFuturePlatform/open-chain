package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets.UTF_8

open class NetworkBlock : Packet() {
    var height: Long = 0
    lateinit var previousHash: String
    lateinit var merkleHash: String
    var timestamp: Long = 0
    var typeId: Int = 0
    lateinit var hash: String
    lateinit var signature: String

    override fun get(buffer: ByteBuf) {
        height = buffer.readLong()
        previousHash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        merkleHash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        timestamp = buffer.readLong()
        typeId = buffer.readInt()
        hash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        signature = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeInt(previousHash.length)
        buffer.writeCharSequence(previousHash, UTF_8)
        buffer.writeInt(merkleHash.length)
        buffer.writeCharSequence(merkleHash, UTF_8)
        buffer.writeLong(timestamp)
        buffer.writeInt(typeId)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)
        buffer.writeInt(signature.length)
        buffer.writeCharSequence(signature, UTF_8)
    }

    override fun toString() = "NetworkBlock(height=$height)"

}
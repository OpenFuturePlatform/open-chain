package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Component
import java.io.Serializable
import java.nio.charset.StandardCharsets.UTF_8

abstract class Packet(
    var version: String? = null,
    var timestamp: Long? = null
) : Serializable {

    private fun read(buffer: ByteBuf) {
        version = readString(buffer)
        timestamp = buffer.readLong()
        readParams(buffer)
    }

    private fun write(buffer: ByteBuf) {
        if (null == version || null == timestamp) {
            throw IllegalStateException("Can't write packet without version or timestamp")
        }
        writeString(buffer, version!!)
        buffer.writeLong(timestamp!!)
        writeParams(buffer)
    }

    protected fun writeString(buffer: ByteBuf, string: String) {
        buffer.writeInt(string.length)
        buffer.writeCharSequence(string, UTF_8)
    }

    protected fun readString(buffer: ByteBuf): String {
        val length = buffer.readInt()
        return buffer.readCharSequence(length, UTF_8).toString()
    }

    protected open fun readParams(buffer: ByteBuf) {}

    protected open fun writeParams(buffer: ByteBuf) {}

    @Component
    class Serializer {

        fun read(buffer: ByteBuf): Packet {
            val id = buffer.readShort()
            val clazz = PacketType.get(id).clazz
            val instance = clazz.java.newInstance()
            instance.read(buffer)
            return instance
        }

        fun write(packet: Packet, buffer: ByteBuf) {
            val id = PacketType.get(packet::class).id
            buffer.writeShort(id.toInt())
            packet.write(buffer)
        }

    }

}

package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

abstract class Packet(
    var version: String? = null,
    var timestamp: Long? = null
) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
        version = readString(buffer)
        timestamp = buffer.readLong()
        readParams(buffer)
    }

    override fun write(buffer: ByteBuf) {
        if (StringUtils.isEmpty(version) || null == timestamp) {
            throw IllegalStateException("Can't write packet without version or timestamp")
        }
        writeString(buffer, version!!)
        buffer.writeLong(timestamp!!)
        writeParams(buffer)
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

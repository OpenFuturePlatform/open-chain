package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@NoArgConstructor
abstract class Packet(
    var version: String? = null,
    var timestamp: Long? = null
) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
        version = buffer.readString()
        timestamp = buffer.readLong()
        readParams(buffer)
    }

    override fun write(buffer: ByteBuf) {
        if (StringUtils.isEmpty(version) || null == timestamp) {
            throw IllegalStateException("Can't write packet without version or timestamp")
        }
        buffer.writeString(version!!)
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

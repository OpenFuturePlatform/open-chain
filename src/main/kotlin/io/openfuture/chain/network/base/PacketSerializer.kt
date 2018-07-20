package io.openfuture.chain.network.base

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.domain.Packet
import io.openfuture.chain.network.domain.PacketType
import org.springframework.stereotype.Component

@Component
class PacketSerializer {

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
package io.openfuture.chain.network.domain.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
data class Packet(
    var data: BaseMessage,
    var version: String,
    var timestamp: Long
) : BaseMessage {

    lateinit var type: PacketType

    override fun read(buffer: ByteBuf) {
        version = buffer.readString()
        timestamp = buffer.readLong()
        type = PacketType.get(buffer.readShort())
        data = type.clazz.java.newInstance()
        data.read(buffer)
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(version)
        buffer.writeLong(timestamp)
        buffer.writeShort(PacketType.get(data).id.toInt())
        data.write(buffer)
    }

}

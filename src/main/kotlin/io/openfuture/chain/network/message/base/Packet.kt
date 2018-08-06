package io.openfuture.chain.network.message.base

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
data class Packet(
    var data: BaseMessage,
    var version: String,
    var timestamp: Long
) : BaseMessage {

    var type: PacketType = PacketType.get(data)


    override fun read(buffer: ByteBuf) {
        version = buffer.readString()
        timestamp = buffer.readLong()
        type = PacketType.get(buffer.readByte())
        data = type.clazz.java.newInstance()
        data.read(buffer)
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(version)
        buffer.writeLong(timestamp)
        buffer.writeByte(PacketType.get(data).id.toInt())
        data.write(buffer)
    }

}

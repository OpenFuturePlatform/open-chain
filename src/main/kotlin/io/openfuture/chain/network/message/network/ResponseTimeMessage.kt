package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class ResponseTimeMessage(
    var originalTime: Long,
    var receiveTime: Long,
    var transmitTime: Long,
    var nodeInfo: NetworkAddress,
    var status: Int
) : Serializable {

    override fun read(buf: ByteBuf) {
        originalTime = buf.readLong()
        receiveTime = buf.readLong()
        transmitTime = buf.readLong()
        status = buf.readInt()
        nodeInfo = NetworkAddress::class.java.getConstructor().newInstance()
        nodeInfo.read(buf)
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(originalTime)
        buf.writeLong(receiveTime)
        buf.writeLong(transmitTime)
        buf.writeInt(status)
        nodeInfo.write(buf)

    }

}
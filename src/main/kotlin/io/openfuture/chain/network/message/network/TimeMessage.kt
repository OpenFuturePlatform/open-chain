package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class TimeMessage (
    var isClient: Boolean = false,
    var originalTime: Long = 0,
    var receiveTime: Long = 0,
    var transmitTime: Long = 0,
    var destinationTime: Long = 0
) : Serializable {

    override fun read(buf: ByteBuf) {
        isClient = buf.readBoolean()
        originalTime = buf.readLong()
        receiveTime = buf.readLong()
        transmitTime = buf.readLong()
        destinationTime = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeBoolean(isClient)
        buf.writeLong(originalTime)
        buf.writeLong(receiveTime)
        buf.writeLong(transmitTime)
        buf.writeLong(destinationTime)
    }

    fun isValidRequest(): Boolean = 0L != originalTime

    fun isValidResponse(): Boolean = 0L != originalTime && 0L != receiveTime && 0L != transmitTime

}
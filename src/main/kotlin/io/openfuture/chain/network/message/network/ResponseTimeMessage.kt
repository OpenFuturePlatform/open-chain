package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class ResponseTimeMessage(
    var nodeTime: Long,
    var networkTime: Long
) : Serializable {

    override fun read(buf: ByteBuf) {
        nodeTime = buf.readLong()
        networkTime = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(nodeTime)
        buf.writeLong(networkTime)
    }

}
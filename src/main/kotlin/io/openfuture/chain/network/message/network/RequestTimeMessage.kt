package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class RequestTimeMessage (
    var originalTime: Long
) : Serializable {

    override fun read(buf: ByteBuf) {
        originalTime = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(originalTime)
    }

}
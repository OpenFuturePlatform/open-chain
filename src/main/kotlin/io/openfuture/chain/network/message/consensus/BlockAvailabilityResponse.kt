package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
class BlockAvailabilityResponse(
    var hash: String,
    var height: Long
): Serializable {

    override fun read(buf: ByteBuf) {
        hash = buf.readString()
        height = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(hash)
        buf.writeLong(height)
    }

}
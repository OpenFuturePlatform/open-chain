package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

class BlockAvailabilityRequest(
    var hash: String
): Serializable {

    override fun read(buf: ByteBuf) {
        hash = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(hash)
    }

}
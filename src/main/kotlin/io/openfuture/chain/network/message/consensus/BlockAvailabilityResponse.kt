package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

class BlockAvailabilityResponse(
    var hash: String,
    var height: Int,
    var publicKey: String
): Serializable {

    override fun read(buf: ByteBuf) {
        hash = buf.readString()
        height = buf.readInt()
        publicKey = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(hash)
        buf.writeInt(height)
        buf.writeString(publicKey)
    }

}
package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class HashBlockResponseMessage(
    var hash: String,
    var synchronizationSessionId: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        hash = buf.readString()
        synchronizationSessionId = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(hash)
        buf.writeString(synchronizationSessionId)
    }

}
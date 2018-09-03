package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class DelegateRequestMessage(
    var synchronizationSessionId: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        synchronizationSessionId = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(synchronizationSessionId)
    }

}

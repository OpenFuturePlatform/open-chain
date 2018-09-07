package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.extension.readSet
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeSet
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class GreetingResponseMessage(
    var externalHost: String,
    var addresses: Set<NetworkAddress>
) : Serializable {

    override fun read(buf: ByteBuf) {
        externalHost = buf.readString()
        addresses = buf.readSet()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(externalHost)
        buf.writeSet(addresses)
    }

}
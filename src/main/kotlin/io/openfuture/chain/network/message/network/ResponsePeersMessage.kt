package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.extension.readSet
import io.openfuture.chain.network.extension.writeSet
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class ResponsePeersMessage(
    var peers: Set<NetworkAddress>
) : Serializable {

    override fun read(buf: ByteBuf) {
        peers = buf.readSet()
    }

    override fun write(buf: ByteBuf) {
        buf.writeSet(peers)
    }

}
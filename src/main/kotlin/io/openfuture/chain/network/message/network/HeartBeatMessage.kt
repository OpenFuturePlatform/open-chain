package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.extension.readSet
import io.openfuture.chain.network.extension.writeSet
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class HeartBeatMessage(
    var explorerAddress: Set<NetworkAddress>,
    var type: Type = PING
) : Serializable {

    override fun read(buf: ByteBuf) {
        type = if (buf.readBoolean()) PING else PONG
        explorerAddress = buf.readSet()
    }

    override fun write(buf: ByteBuf) {
        buf.writeBoolean(type == PING)
        buf.writeSet(explorerAddress)
    }


    enum class Type { PING, PONG }

}

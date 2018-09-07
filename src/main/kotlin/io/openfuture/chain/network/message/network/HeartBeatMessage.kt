package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class HeartBeatMessage(
    var type: Type = PING
) : Serializable {

    override fun read(buf: ByteBuf) {
        type = if (buf.readBoolean()) PING else PONG
    }

    override fun write(buf: ByteBuf) {
        buf.writeBoolean(type == PING)
    }


    enum class Type { PING, PONG }

}

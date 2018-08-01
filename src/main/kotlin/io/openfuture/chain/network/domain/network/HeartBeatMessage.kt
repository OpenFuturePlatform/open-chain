package io.openfuture.chain.network.domain.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.domain.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.domain.network.HeartBeatMessage.Type.PONG

@NoArgConstructor
data class HeartBeatMessage(
    var type: Type
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        type = if (buffer.readBoolean()) PING else PONG
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeBoolean(type == PING)
    }

    enum class Type { PING, PONG }

}

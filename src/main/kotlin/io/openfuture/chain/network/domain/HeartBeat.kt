package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.HeartBeat.Type.PING
import io.openfuture.chain.network.domain.HeartBeat.Type.PONG

@NoArgConstructor
data class HeartBeat(
    var type: Type
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        type = if (buffer.readBoolean()) PING else PONG
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeBoolean(type == PING)
    }

    enum class Type { PING, PONG }

}

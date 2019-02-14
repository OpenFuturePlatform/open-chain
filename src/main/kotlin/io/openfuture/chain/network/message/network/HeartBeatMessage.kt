package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
class HeartBeatMessage : Message {

    override fun read(buf: ByteBuf) {
    }

    override fun write(buf: ByteBuf) {
    }

}

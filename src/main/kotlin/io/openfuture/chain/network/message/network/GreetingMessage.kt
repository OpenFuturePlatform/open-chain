package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
class GreetingMessage(
    var port: Int
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        port = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeInt(port)
    }

}
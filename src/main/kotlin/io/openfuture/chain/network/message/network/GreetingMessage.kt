package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class GreetingMessage(
    var address: NetworkAddressMessage
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        address = NetworkAddressMessage::class.java.getConstructor().newInstance()
        address.read(buffer)
    }

    override fun write(buffer: ByteBuf) {
        address.write(buffer)
    }

}
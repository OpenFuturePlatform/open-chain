package io.openfuture.chain.network.domain.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.domain.network.address.NetworkAddressMessage

@NoArgConstructor
data class GreetingMessage(
    var address: NetworkAddressMessage
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        address = NetworkAddressMessage::class.java.newInstance()
        address.read(buffer)
    }

    override fun write(buffer: ByteBuf) {
        address.write(buffer)
    }

}
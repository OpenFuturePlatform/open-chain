package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.NetworkAddressMessage

@NoArgConstructor
data class DelegateResponseMessage(
    var addresses: List<NetworkAddressMessage>
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        addresses = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeList(addresses)
    }

}
package io.openfuture.chain.network.message.network.address

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

@NoArgConstructor
data class AddressesMessage(
    var values: List<NetworkAddressMessage>
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        values = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeList(values)
    }

}
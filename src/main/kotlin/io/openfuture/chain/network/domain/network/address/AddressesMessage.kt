package io.openfuture.chain.network.domain.network.address

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage
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
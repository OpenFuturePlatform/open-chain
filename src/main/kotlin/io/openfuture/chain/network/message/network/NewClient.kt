package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class NewClient(
    var address: NetworkAddress
) : Serializable {

    override fun read(buf: ByteBuf) {
        address = NetworkAddress::class.java.newInstance()
        address.read(buf)
    }

    override fun write(buf: ByteBuf) {
        address.write(buf)
    }

}
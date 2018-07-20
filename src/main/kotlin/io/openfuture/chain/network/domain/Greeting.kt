package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class Greeting(
    var address: NetworkAddress
) : Packet() {

    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        address = NetworkAddress::class.java.newInstance()
        address.get(buffer)
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        address.send(buffer)
    }

}
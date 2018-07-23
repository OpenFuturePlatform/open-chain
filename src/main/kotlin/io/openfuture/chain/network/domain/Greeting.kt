package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class Greeting(
        var address: NetworkAddress
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        address = NetworkAddress::class.java.newInstance()
        address.read(buffer)
    }

    override fun writeParams(buffer: ByteBuf) {
        address.write(buffer)
    }

}
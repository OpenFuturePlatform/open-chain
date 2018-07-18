package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NonArgConstructor

@NonArgConstructor
data class Greeting(
    var address: NetworkAddress
) : Packet() {

    override fun get(buffer: ByteBuf) {
        address = NetworkAddress::class.java.newInstance()
        address.get(buffer)
    }

    override fun send(buffer: ByteBuf) {
        address.send(buffer)
    }

}
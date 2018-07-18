package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf

class Greeting() : Packet() {

    lateinit var address: NetworkAddress

    constructor(address: NetworkAddress) : this() {
        this.address = address
    }

    override fun get(buffer: ByteBuf) {
        address = NetworkAddress()
        address.get(buffer)
    }

    override fun send(buffer: ByteBuf) {
        address.send(buffer)
    }

    override fun toString(): String {
        return "Greeting(address=$address)"
    }


}
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

    override fun toString() = "Greeting(address=$address)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Greeting

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

}
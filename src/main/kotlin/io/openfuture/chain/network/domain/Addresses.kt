package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf

class Addresses() : Packet() {

    lateinit var values : MutableList<NetworkAddress>

    constructor(values : List<NetworkAddress>) : this(){
        this.values = values.toMutableList()
    }

    override fun get(buffer: ByteBuf) {
        val size = buffer.readInt()
        values = mutableListOf()
        for (index in 1..size) {
            val address = NetworkAddress()
            address.get(buffer)
            values.add(address)
        }
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeInt(values.size)
        for (address in values) {
            address.send(buffer)
        }
    }

    override fun toString(): String {
        return "Addresses(values=$values)"
    }


}
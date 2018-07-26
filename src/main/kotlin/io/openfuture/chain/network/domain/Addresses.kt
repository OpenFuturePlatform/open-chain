package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class Addresses(
    var values: List<NetworkAddress>
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        val size = buffer.readInt()
        val list = mutableListOf<NetworkAddress>()
        for (index in 1..size) {
            val address = NetworkAddress::class.java.newInstance()
            address.read(buffer)
            list.add(address)
        }
        values = list
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeInt(values.size)
        for (address in values) {
            address.write(buffer)
        }
    }

}
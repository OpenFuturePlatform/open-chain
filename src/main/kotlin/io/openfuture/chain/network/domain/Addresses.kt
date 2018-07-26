package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class Addresses(
    var values: List<NetworkAddress>
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        values = readList(buffer)
    }

    override fun writeParams(buffer: ByteBuf) {
        writeList(buffer, values)
    }

}
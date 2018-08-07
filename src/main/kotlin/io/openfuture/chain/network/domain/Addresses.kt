package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

@NoArgConstructor
data class Addresses(
    var values: List<NetworkAddress>
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        values = buffer.readList()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeList(values)
    }

}
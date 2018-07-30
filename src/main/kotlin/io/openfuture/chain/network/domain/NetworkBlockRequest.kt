package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
data class NetworkBlockRequest(
    var hash: String
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        hash = buffer.readString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeString(hash)
    }

}
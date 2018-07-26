package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class NetworkBlockRequest(var hash: String) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        hash = readString(buffer)
    }

    override fun writeParams(buffer: ByteBuf) {
        writeString(buffer, hash)
    }

}
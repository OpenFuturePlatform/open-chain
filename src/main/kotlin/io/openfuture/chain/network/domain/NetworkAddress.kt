package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NonArgConstructor

@NonArgConstructor
data class NetworkAddress(
    var host: String,
    var port: Int
) : Packet() {

    override fun get(buffer: ByteBuf) {
        host = readString(buffer)
        port = buffer.readInt()
    }

    override fun send(buffer: ByteBuf) {
        writeString(buffer, host)
        buffer.writeInt(port)
    }

}
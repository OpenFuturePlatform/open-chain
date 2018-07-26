package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class NetworkAddress(
    var host: String,
    var port: Int
) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
        host = readString(buffer)
        port = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        writeString(buffer, host)
        buffer.writeInt(port)
    }

}
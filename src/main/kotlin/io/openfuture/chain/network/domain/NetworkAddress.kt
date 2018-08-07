package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
data class NetworkAddress(
    var host: String,
    var port: Int
) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
        host = buffer.readString()
        port = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(host)
        buffer.writeInt(port)
    }

}
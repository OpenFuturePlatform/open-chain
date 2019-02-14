package io.openfuture.chain.network.entity

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
data class NetworkAddress(
    var host: String,
    var port: Int
) : Message {

    override fun read(buf: ByteBuf) {
        host = buf.readString()
        port = buf.readInt()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(host)
        buf.writeInt(port)
    }

}
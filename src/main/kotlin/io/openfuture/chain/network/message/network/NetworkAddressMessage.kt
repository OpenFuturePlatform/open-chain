package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class NetworkAddressMessage(
    var host: String,
    var port: Int
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        host = buffer.readString()
        port = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(host)
        buffer.writeInt(port)
    }

}
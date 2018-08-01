package io.openfuture.chain.network.domain.network.address

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

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
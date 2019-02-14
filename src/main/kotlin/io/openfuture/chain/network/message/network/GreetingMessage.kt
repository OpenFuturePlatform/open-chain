package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
data class GreetingMessage(
    var externalPort: Int,
    var uid: String
) : Message {

    override fun read(buf: ByteBuf) {
        externalPort = buf.readInt()
        uid = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeInt(externalPort)
        buf.writeString(uid)
    }

}
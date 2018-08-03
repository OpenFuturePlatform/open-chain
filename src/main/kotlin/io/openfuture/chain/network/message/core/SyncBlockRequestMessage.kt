package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class SyncBlockRequestMessage(
    var hash: String
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        hash = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(hash)
    }

}
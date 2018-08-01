package io.openfuture.chain.network.domain.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.domain.base.BaseMessage

@NoArgConstructor
data class BlockRequestMessage(
    var hash: String
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        hash = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeString(hash)
    }

}
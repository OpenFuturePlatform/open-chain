package io.openfuture.chain.network.domain.network.time

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.base.BaseMessage

@NoArgConstructor
data class AskTimeMessage(
    var nodeTimestamp: Long
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
    }

}
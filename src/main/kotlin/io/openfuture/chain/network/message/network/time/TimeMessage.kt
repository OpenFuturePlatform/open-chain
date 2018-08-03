package io.openfuture.chain.network.message.network.time

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
data class TimeMessage(
    var nodeTimestamp: Long,
    var networkTimestamp: Long
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
        networkTimestamp = buffer.readLong()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
        buffer.writeLong(networkTimestamp)
    }

}

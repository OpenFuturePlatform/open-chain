package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class TimeSyncResponse(
    var nodeTimestamp: Long,
    var networkTimestamp: Long
) : Packet() {

    override fun get(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
        networkTimestamp = buffer.readLong()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
        buffer.writeLong(networkTimestamp)
    }

}

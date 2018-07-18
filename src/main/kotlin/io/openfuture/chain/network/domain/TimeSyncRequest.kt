package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class TimeSyncRequest(
    var nodeTimestamp: Long
) : Packet() {

    override fun get(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
    }

}
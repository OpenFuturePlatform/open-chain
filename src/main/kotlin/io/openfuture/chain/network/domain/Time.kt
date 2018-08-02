package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor

@NoArgConstructor
data class Time(
    var nodeTimestamp: Long,
    var networkTimestamp: Long
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
        networkTimestamp = buffer.readLong()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
        buffer.writeLong(networkTimestamp)
    }

}

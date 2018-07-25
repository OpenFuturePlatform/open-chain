package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class AskTime(
    var nodeTimestamp: Long
) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
    }

}
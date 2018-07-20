package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class AskTime(
    var nodeTimestamp: Long
) : Packet() {

    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        nodeTimestamp = buffer.readLong()
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        buffer.writeLong(nodeTimestamp)
    }

}
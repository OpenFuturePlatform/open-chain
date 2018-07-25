package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import java.nio.charset.StandardCharsets.UTF_8

@NoArgConstructor
data class NetworkBlockRequest(var hash: String) : Packet() {

    override fun readParams(buffer: ByteBuf) {
        hash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)
    }

}
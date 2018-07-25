package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.Delegate
import java.nio.charset.StandardCharsets.UTF_8

@NoArgConstructor
data class NetworkDelegate(
    var host: String,
    var port: Int,
    var rating: Int
) {

    constructor(delegate: Delegate) : this(delegate.host, delegate.port, delegate.rating)

    fun read(buffer: ByteBuf) {
        host = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        port = buffer.readInt()
        rating = buffer.readInt()
    }

    fun write(buffer: ByteBuf) {
        buffer.writeInt(host.length)
        buffer.writeCharSequence(host, UTF_8)
        buffer.writeInt(port)
        buffer.writeInt(rating)
    }

}
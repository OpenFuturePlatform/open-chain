package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.Delegate
import java.nio.charset.StandardCharsets.UTF_8

class NetworkDelegate(
    var host: String? = null,
    var port: Int? = 0,
    var rating: Int? = 0
) : Packet() {

    constructor(delegate: Delegate) : this(delegate.host, delegate.port, delegate.rating)

    override fun get(buffer: ByteBuf) {
        host = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        port = buffer.readInt()
        rating = buffer.readInt()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeInt(host!!.length)
        buffer.writeCharSequence(host, UTF_8)
        buffer.writeInt(port!!)
        buffer.writeInt(rating!!)
    }

    override fun toString() = "NetworkDelegate(host=$host,port=$port)"
}
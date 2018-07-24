package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.Delegate
import java.nio.charset.StandardCharsets

class NetworkDelegate() : Packet() {
    lateinit var host: String
    var port: Int = 0
    var rating: Int = 0

    constructor(delegate: Delegate) : this() {
        this.host = delegate.host
        this.port = delegate.port
        this.rating = delegate.rating
    }

    override fun get(buffer: ByteBuf) {
        host = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString()
        port = buffer.readInt()
        rating = buffer.readInt()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeInt(host.length)
        buffer.writeCharSequence(host, StandardCharsets.UTF_8)
        buffer.writeInt(port)
        buffer.writeInt(rating)
    }

    override fun toString() = "NetworkDelegate(host=$host,port=$port)"
}
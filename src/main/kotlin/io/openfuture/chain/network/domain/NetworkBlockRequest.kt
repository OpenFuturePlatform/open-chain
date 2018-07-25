package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets.UTF_8

class NetworkBlockRequest() : Packet() {
    lateinit var hash: String

    constructor(hash: String) : this() {
        this.hash = hash
    }

    override fun get(buffer: ByteBuf) {
        hash =  buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)    }

    override fun toString() = "NetworkBlockRequest(hash=$hash)"
}
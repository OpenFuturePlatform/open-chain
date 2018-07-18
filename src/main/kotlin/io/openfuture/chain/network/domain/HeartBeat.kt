package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf

class HeartBeat() : Packet() {

    enum class Type {
        PING, PONG
    }

    lateinit var type: Type

    constructor(type: Type) : this() {
        this.type = type
    }

    override fun get(buffer: ByteBuf) {
        type = if (buffer.readBoolean()) Type.PING else Type.PONG
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeBoolean(type == Type.PING)
    }

    override fun toString() = "HeartBeat(type=$type)"

}

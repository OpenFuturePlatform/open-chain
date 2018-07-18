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
        if (buffer.readBoolean()) {
            type = Type.PING
        } else {
            type = Type.PONG
        }
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeBoolean(type == Type.PING)
    }

    override fun toString(): String {
        return "HeartBeat(type=$type)"
    }
}

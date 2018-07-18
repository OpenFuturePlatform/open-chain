package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import kotlin.properties.Delegates

class TimeSyncRequest() : Packet() {

    var nodeTimestamp: Long by Delegates.notNull()

    constructor(nodeTimestamp: Long) : this() {
        this.nodeTimestamp = nodeTimestamp
    }

    override fun get(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
    }

    override fun toString() = "TimeSyncRequest(nodeTimestamp=$nodeTimestamp)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeSyncRequest

        if (nodeTimestamp != other.nodeTimestamp) return false

        return true
    }

    override fun hashCode(): Int {
        return nodeTimestamp.hashCode()
    }

}
package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import kotlin.properties.Delegates

class TimeSyncResponse() : Packet() {

    var nodeTimestamp: Long by Delegates.notNull()
    var networkTimestamp: Long by Delegates.notNull()

    constructor(nodeTimestamp: Long, networkTimestamp: Long) : this() {
        this.nodeTimestamp = nodeTimestamp
        this.networkTimestamp = networkTimestamp
    }

    override fun get(buffer: ByteBuf) {
        nodeTimestamp = buffer.readLong()
        networkTimestamp = buffer.readLong()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(nodeTimestamp)
        buffer.writeLong(networkTimestamp)
    }

    override fun toString() = "TimeSyncResponse(nodeTimestamp=$nodeTimestamp,networkTimestamp=$networkTimestamp)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeSyncResponse

        if (nodeTimestamp != other.nodeTimestamp) return false
        if (networkTimestamp != other.networkTimestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nodeTimestamp.hashCode()
        result = 31 * result + (networkTimestamp.hashCode())
        return result
    }

}

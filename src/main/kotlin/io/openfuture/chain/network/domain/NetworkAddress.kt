package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.properties.Delegates

class NetworkAddress() : Packet() {

    lateinit var host: String
    var port: Int by Delegates.notNull()

    constructor(host : String, port : Int) : this(){
        this.host = host
        this.port = port
    }

    override fun get(buffer: ByteBuf) {
        val length = buffer.readInt()
        host = buffer.readCharSequence(length, UTF_8).toString()
        port = buffer.readInt()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeInt(host.length)
        buffer.writeCharSequence(host, UTF_8)
        buffer.writeInt(port)
    }

    override fun toString() = "NetworkAddress(host=$host,port=$port)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkAddress

        if (host != other.host) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + port
        return result
    }

}
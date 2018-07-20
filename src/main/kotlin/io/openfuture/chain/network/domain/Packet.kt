package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.io.Serializable
import java.nio.charset.StandardCharsets.UTF_8

abstract class Packet(
        var version: String? = null,
        var timestamp: Long? = null
) : Serializable {

    fun read(buffer: ByteBuf) {
        version = readString(buffer)
        timestamp = buffer.readLong()
    }

    fun write(buffer: ByteBuf) {
        if (null == version || null == timestamp) {
            throw IllegalStateException("Can't write packet without version or timestamp")
        }
        writeString(buffer, version!!)
        buffer.writeLong(timestamp!!)
    }

    protected fun writeString(buffer: ByteBuf, string: String) {
        buffer.writeInt(string.length)
        buffer.writeCharSequence(string, UTF_8)
    }

    protected fun readString(buffer: ByteBuf): String {
        val length = buffer.readInt()
        return buffer.readCharSequence(length, UTF_8).toString()
    }

    protected open fun readParams(buffer: ByteBuf) {}

    protected open fun writeParams(buffer: ByteBuf) {}

}

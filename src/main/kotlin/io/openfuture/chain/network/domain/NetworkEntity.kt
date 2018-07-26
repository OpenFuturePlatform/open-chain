package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.io.Serializable
import java.nio.charset.StandardCharsets.UTF_8


abstract class NetworkEntity : Serializable {

    protected fun writeString(buffer: ByteBuf, string: String) {
        buffer.writeInt(string.length)
        buffer.writeCharSequence(string, UTF_8)
    }

    protected fun readString(buffer: ByteBuf): String {
        val length = buffer.readInt()
        return buffer.readCharSequence(length, UTF_8).toString()
    }

    inline fun <reified T : NetworkEntity> readList(buffer: ByteBuf): MutableList<T> {
        val size = buffer.readInt()
        val list = mutableListOf<T>()
        for (index in 1..size) {
            val address = T::class.java.newInstance()
            address.read(buffer)
            list.add(address)
        }
        return list
    }

    fun <T : NetworkEntity> writeList(buffer: ByteBuf, list: List<T>) {
        buffer.writeInt(list.size)
        for (element in list) {
            element.write(buffer)
        }
    }

    abstract fun read(buffer: ByteBuf)

    abstract fun write(buffer: ByteBuf)

}

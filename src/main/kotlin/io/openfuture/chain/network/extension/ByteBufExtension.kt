package io.openfuture.chain.network.extension

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.domain.NetworkEntity
import java.nio.charset.StandardCharsets.UTF_8

fun ByteBuf.writeString(string: String) {
    this.writeInt(string.length)
    this.writeCharSequence(string, UTF_8)
}

fun ByteBuf.readString(): String {
    val length = this.readInt()
    return this.readCharSequence(length, UTF_8).toString()
}

inline fun <reified T : NetworkEntity> ByteBuf.readList(): MutableList<T> {
    val size = this.readInt()
    val list = mutableListOf<T>()
    for (index in 1..size) {
        val address = T::class.java.newInstance()
        address.read(this)
        list.add(address)
    }
    return list
}

fun <T : NetworkEntity> ByteBuf.writeList(list: List<T>) {
    this.writeInt(list.size)
    for (element in list) {
        element.write(this)
    }
}
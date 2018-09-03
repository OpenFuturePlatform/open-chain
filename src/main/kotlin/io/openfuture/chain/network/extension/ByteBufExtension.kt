package io.openfuture.chain.network.extension

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.serialization.Serializable
import java.nio.charset.StandardCharsets.UTF_8

fun ByteBuf.writeString(string: String) {
    this.writeInt(string.length)
    this.writeCharSequence(string, UTF_8)
}

fun ByteBuf.readString(): String = this.readCharSequence(this.readInt(), UTF_8).toString()

fun ByteBuf.readStringList(): List<String> {
    val size = this.readInt()
    val list = mutableListOf<String>()
    for (index in 1..size) {
        list.add(readString())
    }
    return list
}

fun ByteBuf.writeStringList(list: List<String>) {
    this.writeInt(list.size)
    list.forEach { this.writeString(it) }
}

inline fun <reified T : Serializable> ByteBuf.readList(): MutableList<T> {
    val size = this.readInt()
    val list = mutableListOf<T>()
    for (index in 1..size) {
        val instance = T::class.java.newInstance()
        instance.read(this)
        list.add(instance)
    }
    return list
}

fun <T : Serializable> ByteBuf.writeList(list: List<T>) {
    this.writeInt(list.size)
    list.forEach { it.write(this) }
}

inline fun <reified T : Serializable> ByteBuf.readSet(): MutableSet<T> {
    val size = this.readInt()
    val set = mutableSetOf<T>()
    for (index in 1..size) {
        val instance = T::class.java.newInstance()
        instance.read(this)
        set.add(instance)
    }
    return set
}

fun <T : Serializable> ByteBuf.writeSet(set: Set<T>) {
    this.writeInt(set.size)
    set.forEach { it.write(this) }
}
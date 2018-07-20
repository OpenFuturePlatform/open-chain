package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.io.Serializable
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.reflect.KClass

abstract class Packet(
    var version: String? = null,
    var timestamp: Long? = null
) : Serializable {

    companion object {

        fun read(buffer: ByteBuf): Packet {
            val id = buffer.readShort()
            val clazz = PacketType.getById(id).clazz
            val instance = clazz.java.newInstance()
            instance.get(buffer)
            return instance
        }

        fun write(packet: Packet, buffer: ByteBuf) {
            val id = PacketType.getByClass(packet::class).id
            buffer.writeShort(id.toInt())
            packet.send(buffer)
        }

    }


    open fun get(buffer: ByteBuf) {
        if (readExistence(buffer)) {
            version = readString(buffer)
        }
        if (readExistence(buffer)) {
            timestamp = buffer.readLong()
        }
    }

    open fun send(buffer: ByteBuf) {
        writeExistence(buffer, version)
        if (version != null) {
            writeString(buffer, version!!)
        }
        writeExistence(buffer, timestamp)
        if (timestamp != null) {
            buffer.writeLong(timestamp!!)
        }
    }

    fun writeString(buffer: ByteBuf, string: String) {
        buffer.writeInt(string.length)
        buffer.writeCharSequence(string, UTF_8)
    }

    fun readString(buffer: ByteBuf): String {
        val length = buffer.readInt()
        return buffer.readCharSequence(length, UTF_8).toString()
    }

    fun writeExistence(buffer: ByteBuf, nullableObject: Any?) {
        buffer.writeBoolean(nullableObject != null)
    }

    fun readExistence(buffer: ByteBuf): Boolean {
        return buffer.readBoolean()
    }

}

enum class PacketType(val clazz: KClass<out Packet>,
                      val id: Short) {

    ADDRESSES(Addresses::class, 1),
    FIND_ADDRESSES(FindAddresses::class, 2),
    GREETING(Greeting::class, 3),
    HEART_BEAT(HeartBeat::class, 4),
    ASK_TIME(AskTime::class, 5),
    TIME(Time::class, 6);


    companion object {

        fun getById(id: Short) = values().single { id == it.id }

        fun getByClass(id: KClass<out Packet>) = values().single { id == it.clazz }

    }

}

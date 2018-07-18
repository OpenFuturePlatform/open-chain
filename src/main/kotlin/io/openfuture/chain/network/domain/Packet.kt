package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.io.Serializable
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


abstract class Packet : Serializable{

    enum class Type(val clazz: KClass<out Packet>,
                    val id: Int) {

        ADDRESSES(Addresses::class, 1),
        FIND_ADDRESSES(FindAddresses::class, 1),
        GREETING(Greeting::class, 3),
        HEART_BEAT(HeartBeat::class, 4),
        TIME_SYNC_REQUEST(TimeSyncRequest::class, 5),
        TIME_SYNC_RESPONSE(TimeSyncResponse::class, 6);

        companion object {

            fun getById(id: Int) = values().single { id == it.id }

            fun getByClass(id: KClass<out Packet>) = values().single { id == it.clazz }

        }

    }

    companion object {

        fun read(buffer: ByteBuf) : Packet {
            val id = buffer.readInt()
            val clazz = Type.getById(id).clazz
            val instance = clazz.createInstance()
            instance.get(buffer)
            return instance
        }

        fun write(packet: Packet, buffer: ByteBuf) {
            val id = Type.getByClass(packet::class).id
            buffer.writeInt(id)
            packet.send(buffer)
        }

    }

    abstract fun get(buffer: ByteBuf)

    abstract fun send(buffer: ByteBuf)

    fun writeString(buffer: ByteBuf, string: String) {
        buffer.writeInt(string.length)
        buffer.writeCharSequence(string, UTF_8)
    }

    fun readString(buffer: ByteBuf) : String {
        val length = buffer.readInt()
        return buffer.readCharSequence(length, UTF_8).toString()
    }

}
package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.io.IOException
import java.io.Serializable
import kotlin.reflect.full.createInstance


abstract class Packet : Serializable{

    companion object {
        private val idMapping = mapOf(
            Pair(Addresses::class, 1),
            Pair(FindAddresses::class, 2),
            Pair(Greeting::class, 3),
            Pair(HeartBeat::class, 4),
            Pair(TimeSyncRequest::class, 5),
            Pair(TimeSyncResponse::class, 6))

        private val classMapping = idMapping.entries.associateBy({ it.value }) { it.key }


        fun read(buffer: ByteBuf) : Packet {
            val id = buffer.readInt()
            val kClass = classMapping[id]
            kClass ?: throw IOException("Wrong packet type: $kClass")
            val instance = kClass.createInstance()
            instance.get(buffer)
            return instance
        }

        fun write(packet: Packet, buffer: ByteBuf) {
            val id = idMapping[packet::class]
            id ?: throw IOException("Wrong packet ID: $id")
            buffer.writeInt(id)
            packet.send(buffer)
        }
    }

    abstract fun get(buffer: ByteBuf)

    abstract fun send(buffer: ByteBuf)

}
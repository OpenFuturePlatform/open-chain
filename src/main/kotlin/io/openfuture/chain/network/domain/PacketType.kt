package io.openfuture.chain.network.domain

import kotlin.reflect.KClass

enum class PacketType(
        val id: Short,
        val clazz: KClass<out Packet>
) {

    ADDRESSES(1, Addresses::class),
    FIND_ADDRESSES(2, FindAddresses::class),
    GREETING(3, Greeting::class),
    HEART_BEAT(4, HeartBeat::class),
    ASK_TIME(5, AskTime::class),
    TIME(6, Time::class);


    companion object {

        fun get(id: Short) = values().single { id == it.id }

        fun get(clazz: KClass<out Packet>) = values().single { clazz == it.clazz }

    }

}

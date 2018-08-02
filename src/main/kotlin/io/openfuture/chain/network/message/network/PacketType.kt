package io.openfuture.chain.network.message.network

import io.openfuture.chain.network.message.TimeMessage
import io.openfuture.chain.network.message.application.block.BlockRequestMessage
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.address.AddressesMessage
import io.openfuture.chain.network.message.network.address.FindAddressesMessage
import io.openfuture.chain.network.message.network.time.AskTimeMessage
import kotlin.reflect.KClass

enum class PacketType(
    val id: Short,
    val clazz: KClass<out BaseMessage>
) {

    ADDRESSES(1, AddressesMessage::class),
    FIND_ADDRESSES(2, FindAddressesMessage::class),
    GREETING(3, GreetingMessage::class),
    HEART_BEAT(4, HeartBeatMessage::class),
    ASK_TIME(5, AskTimeMessage::class),
    TIME(6, TimeMessage::class),
    SYNC_BLOCKS_REQUEST(7, BlockRequestMessage::class),
    MAIN_BLOCK(8, MainBlockMessage::class),
    GENESIS_BLOCK(9, GenesisBlockMessage::class);


    companion object {

        fun get(id: Short) = values().single { id == it.id }

        fun get(clazz: KClass<out BaseMessage>) = values().single { clazz == it.clazz }

        fun get(packet: BaseMessage) = get(packet::class)

    }

}

package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.application.block.*
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.address.AddressesMessage
import io.openfuture.chain.network.message.network.address.FindAddressesMessage
import io.openfuture.chain.network.message.network.time.AskTimeMessage
import io.openfuture.chain.network.message.network.time.TimeMessage
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
    SYNC_BLOCKS_REQUEST(7, SyncBlockRequestMessage::class),
    MAIN_BLOCK(8, MainBlockMessage::class),
    GENESIS_BLOCK(9, GenesisBlockMessage::class),
    BLOCK_APPROVAL(10, BlockApprovalMessage::class),
    PENDING_BLOCK(11, PendingBlockMessage::class),
    TRANSFER_TRANSACTION(12, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(13, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(14, VoteTransactionMessage::class);


    companion object {

        fun get(id: Short) = values().single { id == it.id }

        fun get(packet: BaseMessage) = values().single { packet::class == it.clazz }

    }

}

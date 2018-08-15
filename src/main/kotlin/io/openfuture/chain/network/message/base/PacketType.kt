package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.*
import kotlin.reflect.KClass

enum class PacketType(
    val id: Byte,
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
    PENDING_BLOCK(11, MainBlockMessage::class),
    TRANSFER_TRANSACTION(12, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(13, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(14, VoteTransactionMessage::class),
    EXPLORER_FIND_ADDRESSES(15, ExplorerFindAddressesMessage::class),
    EXPLORER_ADDRESSES(16, ExplorerAddressesMessage::class),
    HASH_BLOCK_REQUEST(17, HashBlockRequestMessage::class),
    HASH_BLOCK_RESPONSE(18, HashBlockResponseMessage::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(packet: BaseMessage) = values().single { packet::class == it.clazz }

    }

}

package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.*
import kotlin.reflect.KClass

enum class PacketType(
    val id: Byte,
    val clazz: KClass<out BaseMessage>
) {

    // network
    ADDRESSES(1, AddressesMessage::class),
    FIND_ADDRESSES(2, FindAddressesMessage::class),
    GREETING(3, GreetingMessage::class),
    HEART_BEAT(4, HeartBeatMessage::class),
    ASK_TIME(5, AskTimeMessage::class),
    TIME(6, TimeMessage::class),
    EXPLORER_FIND_ADDRESSES(7, ExplorerFindAddressesMessage::class),
    EXPLORER_ADDRESSES(8, ExplorerAddressesMessage::class),
    // consensus
    BLOCK_APPROVAL(9, BlockApprovalMessage::class),
    PENDING_BLOCK(10, PendingBlockMessage::class),
    // blockchain
    MAIN_BLOCK(11, MainBlockMessage::class),
    GENESIS_BLOCK(12, GenesisBlockMessage::class),
    TRANSFER_TRANSACTION(13, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(14, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(15, VoteTransactionMessage::class),
    // sync
    HASH_BLOCK_REQUEST(16, HashBlockRequestMessage::class),
    HASH_BLOCK_RESPONSE(17, HashBlockResponseMessage::class),
    SYNC_BLOCKS_REQUEST(18, SyncBlockRequestMessage::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(packet: BaseMessage) = values().single { packet::class == it.clazz }

    }

}

package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
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
    GREETING_RESPONSE(4, GreetingResponseMessage::class),
    HEART_BEAT(5, HeartBeatMessage::class),
    ASK_TIME(6, AskTimeMessage::class),
    TIME(7, TimeMessage::class),
    EXPLORER_FIND_ADDRESSES(8, ExplorerFindAddressesMessage::class),
    EXPLORER_ADDRESSES(9, ExplorerAddressesMessage::class),
    // consensus
    BLOCK_APPROVAL(10, BlockApprovalMessage::class),
    PENDING_BLOCK(11, PendingBlockMessage::class),
    // blockchain
    TRANSFER_TRANSACTION(12, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(13, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(14, VoteTransactionMessage::class),
    // sync
    HASH_BLOCK_REQUEST(15, HashBlockRequestMessage::class),
    HASH_BLOCK_RESPONSE(16, HashBlockResponseMessage::class),
    SYNC_BLOCKS_REQUEST(17, SyncBlockRequestMessage::class),
    DELEGATE_REQUEST(18, DelegateRequestMessage::class),
    DELEGATE_RESPONSE(19, DelegateResponseMessage::class),
    MAIN_BLOCK(20, MainBlockMessage::class),
    GENESIS_BLOCK(21, GenesisBlockMessage::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(packet: BaseMessage) = values().single { packet::class == it.clazz }

    }

}

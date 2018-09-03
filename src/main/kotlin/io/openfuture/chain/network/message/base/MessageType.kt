package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.network.*
import io.openfuture.chain.network.message.sync.*
import io.openfuture.chain.network.serialization.Serializable
import kotlin.reflect.KClass

enum class MessageType(
    val id: Byte,
    val clazz: KClass<out Serializable>
) {

    // network
    HEART_BEAT(1, HeartBeatMessage::class),
    REQUEST_TIME(2, RequestTimeMessage::class),
    RESPONSE_TIME(3, ResponseTimeMessage::class),
    GREETING(4, GreetingMessage::class),
    GREETING_RESPONSE(5, GreetingResponseMessage::class),
    // core
    TRANSFER_TRANSACTION(6, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(7, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(8, VoteTransactionMessage::class),
    // consensus
    BLOCK_APPROVAL(9, BlockApprovalMessage::class),
    PENDING_BLOCK(10, PendingBlockMessage::class),
    // sync
    HASH_BLOCK_REQUEST(11, HashBlockRequestMessage::class),
    HASH_BLOCK_RESPONSE(12, HashBlockResponseMessage::class),
    SYNC_BLOCKS_REQUEST(13, SyncBlockRequestMessage::class),
    DELEGATE_REQUEST(14, DelegateRequestMessage::class),
    DELEGATE_RESPONSE(15, DelegateResponseMessage::class),
    MAIN_BLOCK(16, MainBlockMessage::class),
    GENESIS_BLOCK(17, GenesisBlockMessage::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(message: Serializable) = values().single { message::class == it.clazz }

    }

}

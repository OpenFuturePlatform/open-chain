package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.NewClient
import io.openfuture.chain.network.message.sync.*
import io.openfuture.chain.network.serialization.Serializable
import kotlin.reflect.KClass

enum class MessageType(
    val id: Byte,
    val clazz: KClass<out Serializable>
) {

    // network
    HEART_BEAT(1, HeartBeatMessage::class),
    GREETING(2, GreetingMessage::class),
    GREETING_RESPONSE(3, GreetingResponseMessage::class),
    NEW_CLIENT(4, NewClient::class),
    // core
    TRANSFER_TRANSACTION(5, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(6, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(7, VoteTransactionMessage::class),
    // consensus
    BLOCK_APPROVAL(8, BlockApprovalMessage::class),
    PENDING_BLOCK(9, PendingBlockMessage::class),
    // sync
    SYNC_REQUEST(10, SyncRequestMessage::class),
    SYNC_RESPONSE(11, SyncResponseMessage::class),
    SYNC_BLOCKS_REQUEST(12, SyncBlockRequestMessage::class),
    MAIN_BLOCK(13, MainBlockMessage::class),
    GENESIS_BLOCK(14, GenesisBlockMessage::class),
    EPOCH_REQUEST(15, EpochRequestMessage::class),
    EPOCH_RESPONSE(16, EpochResponseMessage::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(message: Serializable) = values().single { message::class == it.clazz }

    }

}

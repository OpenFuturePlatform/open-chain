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
    REQUEST_PEERS(5, RequestPeersMessage::class),
    RESPONSE_PEERS(6, ResponsePeersMessage::class),
    EXPLORER_FIND_NODES(8, ExplorerFindNodesMessage::class),
    EXPLORER_NODES(9, ExplorerNodesMessage::class),
    // core
    TRANSFER_TRANSACTION(10, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(11, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(12, VoteTransactionMessage::class),
    // consensus
    BLOCK_APPROVAL(13, BlockApprovalMessage::class),
    PENDING_BLOCK(14, PendingBlockMessage::class),
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

        fun get(message: Serializable) = values().single { message::class == it.clazz }

    }

}

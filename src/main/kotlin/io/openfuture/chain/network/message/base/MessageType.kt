package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.NewClient
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.MainBlockMessage
import kotlin.reflect.KClass

enum class MessageType(
    val id: Byte,
    val clazz: KClass<out Message>
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
    MAIN_BLOCK(10, MainBlockMessage::class),
    GENESIS_BLOCK(11, GenesisBlockMessage::class),
    EPOCH_REQUEST(12, EpochRequestMessage::class),
    EPOCH_RESPONSE(13, EpochResponseMessage::class),
    BLOCK_AVAILABILITY_REQUEST(14, BlockAvailabilityRequest::class),
    BLOCK_AVAILABILITY_RESPONSE(15, BlockAvailabilityResponse::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(message: Message) = values().single { message::class == it.clazz }

    }

}

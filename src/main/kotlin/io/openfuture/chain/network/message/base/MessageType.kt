package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.DeployTransactionMessage
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
    DEPLOY_TRANSACTION(8, DeployTransactionMessage::class),
    // consensus
    BLOCK_APPROVAL(9, BlockApprovalMessage::class),
    PENDING_BLOCK(10, PendingBlockMessage::class),
    // sync
    MAIN_BLOCK(11, MainBlockMessage::class),
    GENESIS_BLOCK(12, GenesisBlockMessage::class),
    EPOCH_REQUEST(13, EpochRequestMessage::class),
    EPOCH_RESPONSE(14, EpochResponseMessage::class),
    BLOCK_AVAILABILITY_REQUEST(15, BlockAvailabilityRequest::class),
    BLOCK_AVAILABILITY_RESPONSE(16, BlockAvailabilityResponse::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(message: Serializable) = values().single { message::class == it.clazz }

    }

}

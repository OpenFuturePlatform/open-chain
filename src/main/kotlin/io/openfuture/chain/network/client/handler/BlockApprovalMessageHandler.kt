package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.block.BlockApprovalMessage
import io.openfuture.chain.consensus.component.block.ObserverStage
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.core.util.DictionaryUtils
import io.openfuture.chain.network.domain.NetworkBlockApprovalMessage
import org.springframework.stereotype.Component

@Component
class BlockApprovalMessageHandler(
    val pendingBlockHandler: PendingBlockHandler
) : ClientHandler<NetworkBlockApprovalMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkBlockApprovalMessage) {
        val messageEntity = BlockApprovalMessage(
            DictionaryUtils.valueOf(ObserverStage::class.java, message.stage.value),
            message.height,
            message.hash,
            message.publicKey
        )
        pendingBlockHandler.handleApproveMessage(messageEntity)
    }

}
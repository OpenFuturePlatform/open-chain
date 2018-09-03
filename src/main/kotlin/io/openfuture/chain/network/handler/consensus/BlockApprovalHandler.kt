package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockApprovalHandler(
    private val pendingBlockHandler: PendingBlockHandler
) : SimpleChannelInboundHandler<BlockApprovalMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockApprovalMessage) {
        pendingBlockHandler.handleApproveMessage(msg)
    }

}
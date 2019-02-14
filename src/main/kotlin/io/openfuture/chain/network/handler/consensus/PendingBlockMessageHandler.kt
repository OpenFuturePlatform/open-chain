package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class PendingBlockMessageHandler(
    private val pendingBlockHandler: PendingBlockHandler
) : SimpleChannelInboundHandler<PendingBlockMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: PendingBlockMessage) {
        pendingBlockHandler.addBlock(msg)
    }

}
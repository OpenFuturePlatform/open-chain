package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockApprovalHandler(
    private val pendingBlockHandler: PendingBlockHandler,
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<BlockApprovalMessage>() {

    companion object {
        private val log = LoggerFactory.getLogger(BlockApprovalHandler::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockApprovalMessage) {
        pendingBlockHandler.handleApproveMessage(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause !is SynchronizationException) {
            log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}")

            channelsHolder.removeChannel(ctx.channel())
        }
    }

}
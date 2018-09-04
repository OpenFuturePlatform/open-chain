package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class ResponseTimeHandler(
    private val nodeClock: NodeClock
) : SimpleChannelInboundHandler<ResponseTimeMessage>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(RequestTimeMessage(nodeClock.nodeTime()))
        super.channelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ResponseTimeMessage) {
        val offset = nodeClock.calculateTimeOffset(msg.nodeTime, msg.networkTime)
        nodeClock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        nodeClock.removeTimeOffset(ctx.channel().remoteAddress().toString())
        super.channelInactive(ctx)
    }

}
package io.openfuture.chain.network.service.message

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.TimeMessage
import io.openfuture.chain.network.message.network.time.AskTimeMessage
import org.springframework.stereotype.Component

@Component
class TimeSyncMessageService(
    private val clock: NodeClock
) {

    fun handleChannelActive(ctx: ChannelHandlerContext) {
        val message = AskTimeMessage(clock.nodeTime())
        ctx.channel().writeAndFlush(message)
    }

    fun handleAskTimeMessage(ctx: ChannelHandlerContext, askTime: AskTimeMessage) {
        ctx.channel().writeAndFlush(TimeMessage(askTime.nodeTimestamp, clock.networkTime()))
    }

    fun handleTimeMessage(ctx: ChannelHandlerContext, message: TimeMessage) {
        val offset = clock.calculateTimeOffset(message.nodeTimestamp, message.networkTimestamp)
        clock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    fun handleChannelInactive(ctx: ChannelHandlerContext) {
        clock.removeTimeOffset(ctx.channel().remoteAddress().toString())
    }

}
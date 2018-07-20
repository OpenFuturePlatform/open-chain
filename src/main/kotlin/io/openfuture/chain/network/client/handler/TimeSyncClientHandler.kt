package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.network.base.handler.BaseHandler
import io.openfuture.chain.network.domain.AskTime
import io.openfuture.chain.network.domain.Time
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncClientHandler(
    private val clock: NodeClock
) : BaseHandler<Time>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val message = AskTime(clock.nodeTime())
        ctx.writeAndFlush(message)
        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Time) {
        val offset = calculateTimeOffset(message)
        clock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        clock.removeTimeOffset(ctx.channel().remoteAddress().toString())
        ctx.fireChannelInactive()
    }

    fun calculateTimeOffset(response: Time): Long {
        val networkLatency = (clock.nodeTime() - response.nodeTimestamp) / 2
        val expectedNetworkTimestamp = response.nodeTimestamp + networkLatency
        return response.networkTimestamp - expectedNetworkTimestamp
    }

}
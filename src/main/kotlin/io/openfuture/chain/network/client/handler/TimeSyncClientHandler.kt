package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.TimeSyncRequest
import io.openfuture.chain.network.domain.TimeSyncResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncClientHandler(
        private val clock: NodeClock
) : BaseHandler<TimeSyncResponse>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val message = TimeSyncRequest(clock.nodeTime())
        ctx.writeAndFlush(message)
        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: TimeSyncResponse) {
        val offset = calculateTimeOffset(message)
        clock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        clock.removeTimeOffset(ctx.channel().remoteAddress().toString())
        ctx.fireChannelInactive()
    }

    fun calculateTimeOffset(response: TimeSyncResponse): Long {
        val networkLatency = (clock.nodeTime() - response.nodeTimestamp) / 2
        val expectedNetworkTimestamp = response.nodeTimestamp + networkLatency
        return response.networkTimestamp - expectedNetworkTimestamp
    }

}
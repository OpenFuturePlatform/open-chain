package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class RequestTimeHandler(
    var nodeClock: NodeClock
) : SimpleChannelInboundHandler<RequestTimeMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: RequestTimeMessage) {
        ctx.writeAndFlush(ResponseTimeMessage(msg.nodeTime, nodeClock.networkTime()))
    }

}
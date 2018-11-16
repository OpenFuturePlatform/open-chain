package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class RequestTimeHandler(
    var clock: Clock
) : SimpleChannelInboundHandler<RequestTimeMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: RequestTimeMessage) {
        val time = clock.currentTimeMillis()
        ctx.writeAndFlush(ResponseTimeMessage(msg.originalTime, time, clock.currentTimeMillis()))
    }

}
package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.Clock
import io.openfuture.chain.network.message.network.TimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class RequestTimeHandler(
    var clock: Clock
) : SimpleChannelInboundHandler<TimeMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TimeMessage) {
        val received = clock.currentTimeMillis()
        if (msg.isValidRequest()) {
            ctx.writeAndFlush(TimeMessage(clock.isSynchronized(), msg.originalTime, received,
                clock.currentTimeMillis()))
        }
    }

}
package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.Clock
import io.openfuture.chain.network.message.network.TimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class TimeHandler(
    var clock: Clock
) : SimpleChannelInboundHandler<TimeMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TimeMessage) {
        val time = clock.currentTimeMillis()
        if (msg.isClient && msg.isValidRequest()) {
            ctx.writeAndFlush(TimeMessage(false, msg.originalTime, time, clock.currentTimeMillis()))
            return
        }

        if (msg.isValidResponse()) {
            msg.destinationTime = time
            clock.adjust(msg)
        }

        ctx.channel().close()
    }

}
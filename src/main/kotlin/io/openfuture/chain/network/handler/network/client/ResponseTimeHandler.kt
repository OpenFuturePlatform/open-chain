package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.Clock
import io.openfuture.chain.network.message.network.TimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class ResponseTimeHandler(
    private val clock: Clock
) : SimpleChannelInboundHandler<TimeMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TimeMessage) {
        msg.destinationTime = clock.currentTimeMillis()
        if (msg.isValidResponse()) {
            clock.adjust(msg)
        }
    }

}
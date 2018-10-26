package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.component.time.ClockSynchronizer
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class ResponseTimeHandler (
    private val clockSynchronizer: ClockSynchronizer,
    private val clock: Clock
) : SimpleChannelInboundHandler<ResponseTimeMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ResponseTimeMessage) {
        val destinationTime = clock.currentTimeMillis()
        clockSynchronizer.add(msg, destinationTime)
        ctx.close()
    }

}
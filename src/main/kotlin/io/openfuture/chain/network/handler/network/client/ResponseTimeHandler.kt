package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.component.time.ClockSynchronizer
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class ResponseTimeHandler (
    private val clockSynchronizer: ClockSynchronizer,
    private val clock: Clock
) : SimpleChannelInboundHandler<ResponseTimeMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ResponseTimeHandler::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ResponseTimeMessage) {
        log.info("Got time sync response from ${ctx.channel().remoteAddress()}")
        val destinationTime = clock.currentTimeMillis()
        clockSynchronizer.add(msg, destinationTime)
        ctx.close()
    }

}
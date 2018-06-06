package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.message.TimeMessageProtos
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ClientHandler : SimpleChannelInboundHandler<TimeMessageProtos.TimeMessage>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TimeMessageProtos.TimeMessage) {
        log.info("Time from server: ${msg.serverTime}")

        val message = msg.toBuilder().setClientTime(System.currentTimeMillis()).build()

        ctx.channel().writeAndFlush(message)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
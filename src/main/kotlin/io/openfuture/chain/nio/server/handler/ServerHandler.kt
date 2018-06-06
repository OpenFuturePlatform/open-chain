package io.openfuture.chain.nio.server.handler

import io.netty.channel.*
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.message.TimeMessageProtos
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ServerHandler : SimpleChannelInboundHandler<TimeMessageProtos.TimeMessage>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is active")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TimeMessageProtos.TimeMessage) {
        log.info("Time from server: ${msg.clientTime}, client: ${msg.serverTime}, difference: " +
                "${msg.clientTime - msg.serverTime}")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        if (evt is IdleStateEvent) {
            val messageBuilder = TimeMessageProtos.TimeMessage.newBuilder()
            val message = messageBuilder.setServerTime(System.currentTimeMillis()).build()
            ctx.channel().writeAndFlush(message)

            log.info("Server Send: ${message.serverTime}")
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is inactive")
    }

}
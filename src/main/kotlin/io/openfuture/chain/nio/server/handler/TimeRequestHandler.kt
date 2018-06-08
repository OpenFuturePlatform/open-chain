package io.openfuture.chain.nio.server.handler

import io.netty.channel.*
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */

@Component
class TimeRequestHandler : SimpleChannelInboundHandler<CommunicationProtocol.TimeRequest>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is active")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: CommunicationProtocol.TimeRequest) {
        log.info("Received time request with time ${msg.time}")
        val time = System.currentTimeMillis()
        val response = CommunicationProtocol.TimeResponse.newBuilder().setTime(time).build()
        ctx.channel().writeAndFlush(response)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is inactive")
    }

}
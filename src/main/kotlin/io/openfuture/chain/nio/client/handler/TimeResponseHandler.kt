package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.nio.server.handler.TimeRequestHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class TimeResponseHandler : SimpleChannelInboundHandler<CommunicationProtocol.TimeResponse>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext,
                              msg: CommunicationProtocol.TimeResponse) {
        log.info("Received time response with time ${msg.time}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Time request is sent")
        val time = System.currentTimeMillis()
        val request = CommunicationProtocol.TimeRequest.newBuilder().setTime(time).build()
        ctx.channel().writeAndFlush(request)
    }
}
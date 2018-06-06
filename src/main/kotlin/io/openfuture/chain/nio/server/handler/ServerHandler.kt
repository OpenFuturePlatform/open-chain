package io.openfuture.chain.nio.server.handler

import io.netty.channel.*
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.nio.server.TcpServer
import io.openfuture.chain.response.GetTimeResponseProto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ServerHandler : SimpleChannelInboundHandler<String>() {

    companion object {
        private val log = LoggerFactory.getLogger(TcpServer::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is active")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        log.info("Server receive: $msg")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        if (evt is IdleStateEvent) {
            val message = GetTimeResponseProto.GetTimeResponse.newBuilder().setCurrentTime(12).build()
            ctx.channel().writeAndFlush(message)
            log.info("Server Send: 12")
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is inactive")
    }

}
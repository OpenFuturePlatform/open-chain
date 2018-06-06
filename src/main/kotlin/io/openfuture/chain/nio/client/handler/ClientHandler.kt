package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.nio.server.TcpServer
import io.openfuture.chain.response.GetTimeResponseProto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ClientHandler : SimpleChannelInboundHandler<GetTimeResponseProto.GetTimeResponse>() {

    companion object {
        private val log = LoggerFactory.getLogger(TcpServer::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GetTimeResponseProto.GetTimeResponse) {
        log.info("From server: ${msg}")
        ctx.channel().writeAndFlush("Pong")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
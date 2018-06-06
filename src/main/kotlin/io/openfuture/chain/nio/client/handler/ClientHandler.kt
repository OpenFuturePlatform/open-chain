package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.nio.server.TcpServer
import io.openfuture.chain.response.GetTimeResponseProto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

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
        val instant = Instant.ofEpochSecond(msg.currentTime.seconds)
        val time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        log.info("From server: $time")
        ctx.channel().writeAndFlush("Pong")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
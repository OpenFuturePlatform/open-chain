package io.openfuture.chain.nio.server.handler

import com.google.protobuf.Timestamp
import io.netty.channel.*
import io.netty.handler.timeout.IdleStateEvent
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
            val seconds = ZonedDateTime.now().toEpochSecond()
            val time = Timestamp.newBuilder().setSeconds(seconds)
            val message = GetTimeResponseProto.GetTimeResponse.newBuilder().setCurrentTime(time).build()
            ctx.channel().writeAndFlush(message)

            val instant = Instant.ofEpochSecond(seconds)
            val loggedTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            log.info("Server Send: $loggedTime")
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is inactive")
    }

}
package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.protocol.CommunicationProtocol.HeartBeat.Type.PING
import io.openfuture.chain.protocol.CommunicationProtocol.HeartBeat.Type.PONG
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class HeartBeatServerHandler : BaseHandler(Type.HEART_BEAT) {

    companion object {
        private val log = LoggerFactory.getLogger(HeartBeatServerHandler::class.java)
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val body = message.heartBeat
        log.info("Heartbeat ({}) from: {}", body.type, ctx.channel().remoteAddress())

        if (body.type == PING) {
            val response = Packet.newBuilder()
                    .setType(Type.HEART_BEAT)
                    .setHeartBeat(HeartBeat.newBuilder().setType(PONG).build())
                    .build()
            ctx.writeAndFlush(response)
        }
    }

}
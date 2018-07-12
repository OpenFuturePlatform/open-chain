package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.HeartBeat
import io.openfuture.chain.protocol.CommunicationProtocol.Packet
import io.openfuture.chain.protocol.CommunicationProtocol.Type.*
import io.openfuture.chain.service.NetworkService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class ConnectionServerHandler : ChannelInboundHandlerAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionServerHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")

        // start heart beat
        val packet = Packet.newBuilder()
            .setType(HEART_BEAT)
            .setHeartBeat(HeartBeat.newBuilder().setType(HeartBeat.Type.PING).build())
            .build()
        ctx.writeAndFlush(packet)

        ctx.fireChannelActive()
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        packet as CommunicationProtocol.Packet

        // check packet type
        val type = packet.type
        when (type) {
            HEART_BEAT -> {}
            TIME_SYNC_REQUEST -> {}
            FIND_PEERS -> {}
            PEERS -> {}
            PEER_RECOGNITION -> {}
            else -> {
                log.error("Illegal packet type: {}", packet)
                ctx.close()
                return
            }
        }

        ctx.fireChannelRead(packet)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} closed")
        ctx.fireChannelInactive()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error", cause)
        ctx.close()
    }

}
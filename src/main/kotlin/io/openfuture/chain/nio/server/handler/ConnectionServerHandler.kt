package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.nio.ChannelStorage
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.HeartBeat
import io.openfuture.chain.protocol.CommunicationProtocol.Packet
import io.openfuture.chain.protocol.CommunicationProtocol.Type.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    private val channels : ChannelStorage
) : ChannelInboundHandlerAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionServerHandler::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val address = ctx.channel().remoteAddress()

        if (check(ctx)) {
            channels.add(ctx.channel())

            log.info("Connection with {} established", address)

            // start heart beat
            val packet = Packet.newBuilder()
                    .setType(HEART_BEAT)
                    .setHeartBeat(HeartBeat.newBuilder().setType(HeartBeat.Type.PING).build())
                    .build()
            ctx.writeAndFlush(packet)

            ctx.fireChannelActive()
        } else {
            log.error("Connection with {} rejected", address)
            ctx.close()
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        packet as CommunicationProtocol.Packet

        // check packet type
        val type = packet.type
        when (type) {
            HEART_BEAT -> {}
            TIME_SYNC_REQUEST -> {}
            JOIN_NETWORK_REQUEST -> {}
            UPDATE_NETWORK_ADDRESSES -> {}
            else -> {
                log.error("Illegal packet type: {}", packet)
                ctx.close()
                return
            }
        }

        ctx.fireChannelRead(packet)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        channels.remove(ctx.channel())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error", cause)
        ctx.close()
    }

    private fun check(ctx: ChannelHandlerContext): Boolean {
        log.trace("Check {}", ctx.channel().remoteAddress())
        return true
    }

}
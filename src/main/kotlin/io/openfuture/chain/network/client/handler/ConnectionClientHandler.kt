package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.ChannelAttributes.REMOTE_PEER
import io.openfuture.chain.network.server.handler.ConnectionServerHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.Type
import io.openfuture.chain.service.NetworkService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class ConnectionClientHandler(
    private val networkService: NetworkService
) : ChannelInboundHandlerAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection established")
        networkService.addConnectedPeer(ctx.channel(), ctx.channel().attr(REMOTE_PEER).get())
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Connection closed")
        networkService.removeConnectedPeer(ctx.channel())
        ctx.fireChannelInactive()
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        packet as CommunicationProtocol.Packet

        // check packet type
        val type = packet.type
        when (type) {
            Type.HEART_BEAT -> {}
            Type.TIME_SYNC_RESPONSE -> {}
            Type.GET_PEER_RESPONSE -> {}
            Type.GET_PEER_REQUEST -> {}
            else -> {
                log.error("Illegal packet type: {}", packet)
                ctx.close()
                return
            }
        }

        ctx.fireChannelRead(packet)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error", cause)
        ctx.close()
    }

}
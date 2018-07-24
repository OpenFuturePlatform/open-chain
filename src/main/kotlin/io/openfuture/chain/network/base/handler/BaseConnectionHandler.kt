package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.domain.Packet
import io.openfuture.chain.network.domain.PacketType
import org.slf4j.LoggerFactory

abstract class BaseConnectionHandler(
    private val allowableTypes: Set<PacketType>
) : ChannelInboundHandlerAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")
        ctx.fireChannelActive()
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        val type = PacketType.get(packet as Packet)
        if (!allowableTypes.contains(type)) {
            log.error("Illegal packet type: $type")
            ctx.close()
            return
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
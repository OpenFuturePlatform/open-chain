package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.domain.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class ConnectionServerHandler : ChannelInboundHandlerAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionServerHandler::class.java)
    }

    private val allowablePacketTypes = setOf(
        Addresses::class,
        FindAddresses::class,
        Greeting::class,
        HeartBeat::class,
        TimeSyncRequest::class)


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")

        // start heart beat
        ctx.writeAndFlush(HeartBeat(HeartBeat.Type.PING))

        ctx.fireChannelActive()
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        log.info("Message received $packet from ${ctx.channel().remoteAddress()}")

        // check packet type
        if (!allowablePacketTypes.contains(packet::class)) {
            log.error("Illegal packet type: ${packet::class}")
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
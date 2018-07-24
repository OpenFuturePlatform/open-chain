package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.domain.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class ConnectionClientHandler : ChannelInboundHandlerAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    private val allowablePacketTypes = setOf(
        Addresses::class,
        FindAddresses::class,
        Greeting::class,
        HeartBeat::class,
        TimeSyncResponse::class,
        NetworkMainBlock::class,
        NetworkGenesisBlock::class)


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")

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
package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class ConnectionHandler(
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<Serializable>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: Serializable) {
        ctx.fireChannelRead(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        channelsHolder.removeChannel(ctx.channel())
        log.debug("CHANNEL Inactive")
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}")

        channelsHolder.removeChannel(ctx.channel())
    }

}

package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.component.ChannelsHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class ConnectionHandler(
    private val channelsHolder: ChannelsHolder
) : ChannelInboundHandlerAdapter() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.debug("${ctx.channel().remoteAddress()} disconnected, operating peers count: ${channelsHolder.size()}")
        channelsHolder.findNewPeer()
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}")
    }

}

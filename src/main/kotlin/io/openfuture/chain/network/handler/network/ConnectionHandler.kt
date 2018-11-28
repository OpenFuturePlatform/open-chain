package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class ConnectionHandler(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService
) : ChannelInboundHandlerAdapter() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    }

    private val channels = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

    override fun channelActive(ctx: ChannelHandlerContext) {
        channels.add(ctx.channel())
        log.info("${ctx.channel().remoteAddress()} connected, operating peers count is ${channelsHolder.size()}, peers count: ${channels.size}")
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.warn("${ctx.channel().remoteAddress()} disconnected, operating peers count is ${channelsHolder.size()}, peers count: ${channels.size}")
        channels.remove(ctx.channel())
        connectionService.findNewPeer()
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}")
        connectionService.findNewPeer()
    }

}

package io.openfuture.chain.network.handler.client

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.handler.base.BaseConnectionHandler
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionClientHandler : BaseConnectionHandler() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        networkService.onClientChannelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        networkService.onClientChannelInactive(ctx)
    }

}
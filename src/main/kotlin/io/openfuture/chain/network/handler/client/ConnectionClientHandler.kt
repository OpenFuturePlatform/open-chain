package io.openfuture.chain.network.handler.client

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.handler.base.BaseConnectionHandler
import io.openfuture.chain.network.service.DefaultApplicationMessageService
import io.openfuture.chain.network.service.NetworkMessageService
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionClientHandler(
    networkService: NetworkMessageService,
    applicationService: DefaultApplicationMessageService
) : BaseConnectionHandler(networkService, applicationService) {

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        networkService.onClientChannelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        networkService.onClientChannelInactive(ctx)
    }

}
package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.message.network.NewClient
import org.springframework.stereotype.Component

@Component
@Sharable
class NewClientHandler(
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : SimpleChannelInboundHandler<NewClient>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: NewClient) {
        explorerAddressesHolder.addNodeInfo(msg.nodeInfo)
    }

}
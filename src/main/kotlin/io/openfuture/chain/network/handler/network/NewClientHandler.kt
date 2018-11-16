package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.message.network.NewClient
import org.springframework.stereotype.Component

@Component
@Sharable
class NewClientHandler(
    private val explorerAddressesHolder: ExplorerAddressesHolder,
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<NewClient>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: NewClient) {
        val nodeInfo = msg.nodeInfo
        if (nodeInfo != explorerAddressesHolder.me && !explorerAddressesHolder.hasNodeInfo(nodeInfo)) {
            explorerAddressesHolder.addNodeInfo(nodeInfo)
            channelsHolder.broadcast(msg)
        }
    }

}
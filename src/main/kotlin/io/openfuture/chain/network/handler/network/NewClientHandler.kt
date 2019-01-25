package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.network.NewClient
import org.springframework.stereotype.Component

@Component
@Sharable
class NewClientHandler(
    private val addressesHolder: AddressesHolder,
    private val channelsHolder: ChannelsHolder,
    private val nodeKeyHolder: NodeKeyHolder
) : SimpleChannelInboundHandler<NewClient>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: NewClient) {
        val nodeInfo = msg.nodeInfo
        if (nodeInfo.uid != nodeKeyHolder.getPublicKeyAsHexString() && !addressesHolder.hasNodeInfo(nodeInfo)) {
            addressesHolder.addNodeInfo(nodeInfo)
            channelsHolder.broadcast(msg)
            channelsHolder.findNewPeer()
        }
    }

}
package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.message.network.NewClient
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingHandler(
    private val nodeKeyHolder: NodeKeyHolder,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder,
    private val nodeProperties: NodeProperties
) : SimpleChannelInboundHandler<GreetingMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingMessage) {
        val hostAddress = (ctx.channel().remoteAddress() as InetSocketAddress).address.hostAddress
        val nodeInfo = NodeInfo(msg.uid, NetworkAddress(hostAddress, msg.externalPort))
        when {
            channelHolder.getNodesInfo().any { it.uid == msg.uid } -> {
                ctx.close()
            }
            nodeProperties.allowedConnections < channelHolder.size() -> {
                val response = GreetingResponseMessage(nodeKeyHolder.getUid(), hostAddress,
                    addressesHolder.getNodesInfo(), false)
                ctx.writeAndFlush(response)
                ctx.close()
            }
            else -> {
                val response = GreetingResponseMessage(nodeKeyHolder.getUid(), hostAddress,
                    addressesHolder.getNodesInfo())
                ctx.writeAndFlush(response)
                addressesHolder.addNodeInfo(nodeInfo)
                channelHolder.addChannel(ctx.channel(), nodeInfo)
                channelHolder.broadcast(NewClient(nodeInfo))
            }
        }
    }

}
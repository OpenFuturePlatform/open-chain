package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingResponseHandler(
    private val config: NodeConfigurator,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder
) : SimpleChannelInboundHandler<GreetingResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingResponseMessage) {
        val channel = ctx.channel()
        val socket = channel.remoteAddress() as InetSocketAddress

        config.setExternalHost(msg.externalHost)
        val address = NetworkAddress(socket.address.hostAddress, socket.port)
        val nodeInfo = NodeInfo(msg.uid, address)
        if (msg.accepted) {
            channelHolder.addChannel(channel, nodeInfo)
        } else {
            addressesHolder.markRejected(nodeInfo)
        }
        addressesHolder.addNodesInfo(msg.nodesInfo)
    }

}
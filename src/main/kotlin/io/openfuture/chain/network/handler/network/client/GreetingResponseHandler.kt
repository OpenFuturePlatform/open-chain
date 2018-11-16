package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.service.ConnectionService
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingResponseHandler(
    private val config: NodeConfigurator,
    private val channelHolder: ChannelsHolder,
    private val addressesHolder: AddressesHolder,
    private val connectionService: ConnectionService
) : SimpleChannelInboundHandler<GreetingResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingResponseMessage) {
        val channel = ctx.channel()
        val socket = channel.remoteAddress() as InetSocketAddress

        config.setExternalHost(msg.externalHost)
        if (msg.accepted) {
            val address = NetworkAddress(socket.address.hostAddress, socket.port)
            channelHolder.addChannel(channel, NodeInfo(msg.uid, address))
        } else {
            // TODO reject
        }
        addressesHolder.addNodesInfo(msg.nodesInfo)
        ctx.executor().submit { connectionService.findNewPeer() }
    }

}
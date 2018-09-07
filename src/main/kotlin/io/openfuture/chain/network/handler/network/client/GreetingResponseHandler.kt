package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingResponseHandler(
    private val config: NodeConfigurator,
    private val nodeKeyHolder: NodeKeyHolder,
    private val channelHolder: ChannelsHolder,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : SimpleChannelInboundHandler<GreetingResponseMessage>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        val socket = channel.remoteAddress() as InetSocketAddress

        channelHolder.addChannel(channel, NetworkAddress(socket.address.hostAddress, socket.port))

        channel.writeAndFlush(GreetingMessage(config.getConfig().externalPort, nodeKeyHolder.getUid()))
        super.channelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingResponseMessage) {
        config.getConfig().externalHost = msg.externalHost
        explorerAddressesHolder.addAddresses(msg.addresses)
    }

}
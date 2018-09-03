package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingHandler(
    private val nodeKeyHolder: NodeKeyHolder,
    private val channelHolder: ChannelsHolder
) : SimpleChannelInboundHandler<GreetingMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingMessage) {
        val channel = ctx.channel()
        val hostAddress = (channel.remoteAddress() as InetSocketAddress).address.hostAddress
        val address = NetworkAddress(hostAddress, msg.externalPort)

        if (nodeKeyHolder.getUid() == msg.uid || channelHolder.getAllAddresses().any { it == address }) {
            ctx.close()
            return
        }

        channelHolder.addClient(channel, address)

        ctx.writeAndFlush(GreetingResponseMessage(hostAddress))
    }

}
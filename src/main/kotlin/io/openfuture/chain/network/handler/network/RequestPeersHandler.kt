package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.network.RequestPeersMessage
import io.openfuture.chain.network.message.network.ResponsePeersMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class RequestPeersHandler(
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<RequestPeersMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: RequestPeersMessage) {
        ctx.writeAndFlush(ResponsePeersMessage(channelsHolder.getAllAddresses().toSet()))
    }

}
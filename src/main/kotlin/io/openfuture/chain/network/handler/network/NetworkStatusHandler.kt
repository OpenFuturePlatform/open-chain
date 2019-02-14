package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.base.Message
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.stereotype.Component

@Component
@Sharable
class NetworkStatusHandler(
    private val channelsHolder: ChannelsHolder,
    private val nodeProperties: NodeProperties
) : SimpleChannelInboundHandler<Message>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        if (channelsHolder.size() >= nodeProperties.peersNumber!! / 2) {
            ctx.fireChannelRead(msg)
        }
    }

}
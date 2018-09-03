package io.openfuture.chain.network.handler.network.client

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeConfigurator
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class GreetingResponseHandler(
    private val config: NodeConfigurator
) : SimpleChannelInboundHandler<GreetingResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingResponseMessage) {
        config.getConfig().externalHost = msg.externalHost
    }

}
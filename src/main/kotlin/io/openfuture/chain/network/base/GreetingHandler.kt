package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Greeting
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GreetingHandler(
    private val networkService: NetworkService,
    private val properties: NodeProperties
) : BaseHandler<Greeting>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Greeting(NetworkAddress(properties.host!!, properties.port!!)))
        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Greeting) {
        networkService.addConnection(ctx.channel(), message.address)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService.removeConnection(ctx.channel())
        ctx.fireChannelInactive()
    }

}
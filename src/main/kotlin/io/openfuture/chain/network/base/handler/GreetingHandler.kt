package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Greeting
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.NetworkService
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class GreetingHandler(
        private val networkService: NetworkService,
        private val properties: NodeProperties
) : CommonHandler<Greeting>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Greeting(NetworkAddress(properties.host!!, properties.port!!)))
        ctx.fireChannelActive()
    }

    override fun channelRead(ctx: ChannelHandlerContext, message: Greeting) {
        networkService.addConnection(ctx.channel(), message.address)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService.removeConnection(ctx.channel())
        ctx.fireChannelInactive()
    }

}
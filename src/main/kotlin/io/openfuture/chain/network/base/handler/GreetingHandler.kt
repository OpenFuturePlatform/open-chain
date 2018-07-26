package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Greeting
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.property.NodeProperty
import io.openfuture.chain.service.ConnectionService
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class GreetingHandler(
    private val service: ConnectionService,
    private val property: NodeProperty
) : CommonHandler<Greeting>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Greeting(NetworkAddress(property.host!!, property.port!!)))
        ctx.fireChannelActive()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, message: Greeting) {
        service.addConnection(ctx.channel(), message.address)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        service.removeConnection(ctx.channel())
        ctx.fireChannelInactive()
    }

}
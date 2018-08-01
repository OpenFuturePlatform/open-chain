package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Addresses
import io.openfuture.chain.network.domain.FindAddresses
import io.openfuture.chain.network.service.ConnectionService
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class AddressDiscoveryHandler(
    private val service: ConnectionService
) : CommonHandler<FindAddresses>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: FindAddresses) {
        ctx.writeAndFlush(Addresses(service.getConnectionAddresses().toList()))
    }

}
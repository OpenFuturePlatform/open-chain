package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Addresses
import io.openfuture.chain.network.domain.FindAddresses
import io.openfuture.chain.service.NetworkService
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class AddressDiscoveryHandler(
        private val networkService: NetworkService
) : CommonHandler<FindAddresses>() {

    override fun channelRead(ctx: ChannelHandlerContext, message: FindAddresses) {
        ctx.writeAndFlush(Addresses(networkService.getConnections().toList()))
    }

}
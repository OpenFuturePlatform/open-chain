package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Addresses
import io.openfuture.chain.network.domain.FindAddresses
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class AddressDiscoveryHandler(
    private val networkService: NetworkService
) : BaseHandler<FindAddresses>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: FindAddresses) {
        ctx.writeAndFlush(Addresses(networkService.getConnections().toList()))
    }

}
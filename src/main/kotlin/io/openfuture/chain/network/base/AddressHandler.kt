package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Addresses
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class AddressHandler(
    private val networkService: NetworkService
) : BaseHandler<Addresses>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: Addresses) {
        networkService.connect(message.values)
    }

}
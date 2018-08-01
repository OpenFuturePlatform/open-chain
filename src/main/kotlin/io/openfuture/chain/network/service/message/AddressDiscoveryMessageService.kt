package io.openfuture.chain.network.service.message

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.network.address.AddressesMessage
import io.openfuture.chain.network.domain.network.address.FindAddressesMessage
import io.openfuture.chain.network.service.ConnectionService
import io.openfuture.chain.network.service.NetworkService
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class AddressDiscoveryMessageService(
    @Lazy
    private val networkService: NetworkService,

    private val connectionService: ConnectionService
) {

    fun handleFindAddressMessage(ctx: ChannelHandlerContext, message: FindAddressesMessage) {
        ctx.writeAndFlush(AddressesMessage(connectionService.getConnectionAddresses().toList()))
    }

    fun handleAddressMessage(ctx: ChannelHandlerContext, message: AddressesMessage) {
        networkService.connect(message.values)
    }

}
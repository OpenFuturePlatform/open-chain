package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.NetworkAddress
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.protocol.CommunicationProtocol.UpdateNetworkAddresses.Type.*
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.NetworkAddressService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class UpdateNetworkAddressesHandler(
    private val addressService: NetworkAddressService,
    private val networkService: NetworkService
) : BaseHandler(Type.UPDATE_NETWORK_ADDRESSES) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val payload = message.updateNetworkAddresses

        if (payload.type == ADD) {
            val newAddress = payload.addAddress.address
            val optionalAddress = addressService.findByNodeId(newAddress.nodeId)
            if (optionalAddress == null) {
                addressService.save(NetworkAddress(newAddress.nodeId, newAddress.host, newAddress.port))
                networkService.broadcast(message)
            }
        }

        if (payload.type == REMOVE) {
            val id = message.updateNetworkAddresses.removeAddress.nodeId
            val optionalAddress = addressService.findByNodeId(id)
            if (optionalAddress != null) {
                addressService.deleteByNodeId(id)
                networkService.broadcast(message)
            }
        }
    }
}
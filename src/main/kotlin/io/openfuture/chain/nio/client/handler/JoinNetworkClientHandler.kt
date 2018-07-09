package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.NodeAttributes
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkAddressService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class JoinNetworkClientHandler(
    private val addressService: NetworkAddressService,
    private val nodeAttributes: NodeAttributes
) : BaseHandler(CommunicationProtocol.Type.JOIN_NETWORK_RESPONSE) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        addressService.deleteAll()
        addressService.saveAll(message.joinNetworkResponse.networkAddressesList)

        nodeAttributes.host = message.joinNetworkResponse.host
        nodeAttributes.id = message.joinNetworkResponse.nodeId

        ctx.channel().close()
    }

}
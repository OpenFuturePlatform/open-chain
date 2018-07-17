package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class AddressDiscoveryHandler(
    private val networkService: NetworkService
) : BaseHandler(Type.FIND_ADDRESSES) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val peers = networkService.getConnections().map { NetworkAddress.newBuilder().setHost(it.host).setPort(it.port).build() }

        val response = Packet.newBuilder()
            .setType(Type.ADDRESSES)
            .setAddresses(Addresses.newBuilder().addAllValues(peers).build())
            .build()

        ctx.writeAndFlush(response)
    }

}
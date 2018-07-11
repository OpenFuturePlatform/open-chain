package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.PeerService
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class JoinNetworkClientHandler(
    private val peerService: PeerService,
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.JOIN_NETWORK_RESPONSE) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        peerService.deleteAll()
        peerService.saveAll(message.joinNetworkResponse.peersList)

        networkService.setNetworkId(message.joinNetworkResponse.networkId)
        networkService.disconnect(ctx.channel())
    }

}
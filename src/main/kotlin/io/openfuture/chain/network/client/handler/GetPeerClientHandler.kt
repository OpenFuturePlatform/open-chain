package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GetPeerClientHandler(
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.GET_PEER_RESPONSE) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        networkService.addKnownPeers(message.getPeerResponse.peersList)
    }

}
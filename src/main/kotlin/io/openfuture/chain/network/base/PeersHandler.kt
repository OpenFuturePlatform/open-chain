package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class PeersHandler(
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.PEERS) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        networkService.addKnownPeers(message.peers.peersList)
    }

}
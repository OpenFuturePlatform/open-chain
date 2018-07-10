package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Peer
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.protocol.CommunicationProtocol.PeerEvent.Type.*
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.PeerService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class PeerEventHandler(
    private val peerService: PeerService,
    private val networkService: NetworkService
) : BaseHandler(Type.PEER_EVENT) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val payload = message.peerEvent

        val address = payload.peer
        val optionalPeer = peerService.findByNetworkId(address.networkId)
        if (payload.type == JOIN_NETWORK && optionalPeer == null) {
            peerService.save(Peer(address.networkId, address.host, address.port))
            networkService.broadcast(message)
        }
        if (payload.type == LEAVE_NETWORK && optionalPeer != null) {
            peerService.deleteByNetworkId(address.networkId)
            networkService.broadcast(message)
        }
    }
}
package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.ChannelAttributes
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.PeerService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class DisconnectHandler(
    private val peerService: PeerService,
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.DISCONNECT){

    private var leaveNetwork = true

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val networkId = ctx.channel().attr(ChannelAttributes.REMOTE_NETWORK_ID).get()
        if (leaveNetwork && networkId != null) {
            peerService.deleteByNetworkId(networkId)
            networkService.broadcast(createMessageToRemoveNetworkAddress(networkId))
        }
        ctx.fireChannelInactive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        leaveNetwork = message.disconnect.leaveNetwork
    }

    private fun createMessageToRemoveNetworkAddress(id : String) : CommunicationProtocol.Packet {
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.PEER_EVENT)
            .setPeerEvent(CommunicationProtocol.PeerEvent.newBuilder()
                .setType(CommunicationProtocol.PeerEvent.Type.LEAVE_NETWORK)
                .setPeer(CommunicationProtocol.Peer.newBuilder()
                    .setNetworkId(id)
                    .build()))
            .build()
    }
}
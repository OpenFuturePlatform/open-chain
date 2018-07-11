package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GetPeerServerHandler(
    private val networkService: NetworkService
) : BaseHandler(Type.GET_PEER_REQUEST) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val peers = ArrayList<CommunicationProtocol.Peer>()

        networkService.connectedPeers().forEach {
            peers.add(CommunicationProtocol.Peer.newBuilder()
                .setNetworkId(it.networkId)
                .setHost(it.host)
                .setPort(it.port)
                .build())
        }

        val response = Packet.newBuilder()
            .setType(Type.GET_PEER_RESPONSE)
            .setGetPeerResponse(GetPeerResponse.newBuilder()
                .addAllPeers(peers)
                .build())
            .build()

        ctx.writeAndFlush(response)
    }

}
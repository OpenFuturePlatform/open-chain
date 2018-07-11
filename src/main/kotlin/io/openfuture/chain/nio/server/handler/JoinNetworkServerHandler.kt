package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Peer
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.PeerService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Scope("prototype")
class JoinNetworkServerHandler(
    private val peerService: PeerService,
    private val networkService: NetworkService
) : BaseHandler(Type.JOIN_NETWORK_REQUEST) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val clientHost = (ctx.channel().remoteAddress() as InetSocketAddress).hostName
        val clientPort = message.joinNetworkRequest.port
        val clientNetworkId = clientHost + clientPort
        peerService.save(Peer(clientNetworkId, clientHost, clientPort))

        networkService.broadcast(createMessageToAddNetworkAddress(clientNetworkId, clientHost, clientPort))

        val response = Packet.newBuilder()
            .setType(Type.JOIN_NETWORK_RESPONSE)
            .setJoinNetworkResponse(JoinNetworkResponse.newBuilder()
                .addAllPeers(peerService.findAll())
                .setNetworkId(clientNetworkId)
                .build())
            .build()
        ctx.writeAndFlush(response)
    }

    private fun createMessageToAddNetworkAddress(id : String, host: String, port: Int)
        : CommunicationProtocol.Packet {
        return Packet.newBuilder()
            .setType(Type.PEER_EVENT)
            .setPeerEvent(PeerEvent.newBuilder()
                .setType(PeerEvent.Type.JOIN_NETWORK)
                .setPeer(CommunicationProtocol.Peer.newBuilder()
                        .setNetworkId(id)
                        .setHost(host)
                        .setPort(port)
                        .build())
                    .build())
            .build()
    }

}
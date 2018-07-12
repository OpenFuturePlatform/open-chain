package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.Peer
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class HandshakeHandler(
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.HANDSHAKE) {

    companion object {
        private val log = LoggerFactory.getLogger(HandshakeHandler::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val peer = networkService.getOwnPeerInfo()

        val message = CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.HANDSHAKE)
            .setHandshake(CommunicationProtocol.Handshake.newBuilder()
                .setPeer(CommunicationProtocol.Peer.newBuilder()
                    .setNetworkId(peer.networkId)
                    .setHost(peer.host)
                    .setPort(peer.port))
                .build())
            .build()
        ctx.writeAndFlush(message)

        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val messagePeer = message.handshake.peer
        val peer = Peer(messagePeer.networkId, messagePeer.host, messagePeer.port)
        networkService.addConnectedPeer(ctx.channel(), peer)
        log.info("Connection with $peer established")
    }

}
package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.ChannelAttributes
import io.openfuture.chain.network.domain.Peer
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class ConnectHandler(
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.CONNECT) {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val peer = networkService.getPeerInfo()

        val message = CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.CONNECT)
            .setConnect(CommunicationProtocol.Connect.newBuilder()
                .setPeer(CommunicationProtocol.Peer.newBuilder()
                    .setNetworkId(peer.networkId)
                    .setHost(peer.host)
                    .setPort(peer.port))
                .build())
            .build()
        ctx.writeAndFlush(message)
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val messagePeer = message.connect.peer
        val peer = Peer(messagePeer.networkId, messagePeer.host, messagePeer.port)
        ctx.channel().attr(ChannelAttributes.REMOTE_PEER).set(peer)

        ctx.fireChannelActive()
    }
}
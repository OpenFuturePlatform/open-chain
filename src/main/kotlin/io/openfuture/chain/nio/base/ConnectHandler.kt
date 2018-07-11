package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.ChannelAttributes
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
        if (networkService.getNetworkId() != null) {
            val message = CommunicationProtocol.Packet.newBuilder()
                .setType(CommunicationProtocol.Type.CONNECT)
                .setConnect(CommunicationProtocol.Connect.newBuilder()
                    .setNetworkId(networkService.getNetworkId())
                    .build())
                .build()
            ctx.writeAndFlush(message)
        }
        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        ctx.channel().attr(ChannelAttributes.REMOTE_NETWORK_ID).set(message.connect.networkId)
    }
}
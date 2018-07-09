package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.ChannelAttributes
import io.openfuture.chain.nio.NodeAttributes
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class ConnectHandler(
    private val attributes: NodeAttributes
) : BaseHandler(CommunicationProtocol.Type.CONNECT) {

    override fun channelActive(ctx: ChannelHandlerContext) {
        if (attributes.networkId != null) {
            val message = CommunicationProtocol.Packet.newBuilder()
                .setType(CommunicationProtocol.Type.CONNECT)
                .setConnect(CommunicationProtocol.Connect.newBuilder()
                    .setNetworkId(attributes.networkId)
                    .build())
                .build()
            ctx.writeAndFlush(message)
        }
        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val payload = message.connect
        ctx.channel().attr(ChannelAttributes.REMOTE_NODE_ID).set(payload.networkId)
    }
}
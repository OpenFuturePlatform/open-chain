package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.util.ReferenceCountUtil
import io.openfuture.chain.nio.base.HandshakeHandler
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class HandshakeServerHandler(
    properties: NodeProperties
) : HandshakeHandler(properties) {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        msg as CommunicationProtocol.Packet
        try {
            if (msg.type != CommunicationProtocol.Type.HANDSHAKE) {
                return
            }
            if (isValid(msg.handshake)) {
                sendHandshakeMessage(ctx)
                handleSuccessfulHandshake(ctx, msg.handshake)
            } else {
                handleFailedHandshake(ctx)
            }
        } finally {
            ReferenceCountUtil.release(msg)
        }
    }

}
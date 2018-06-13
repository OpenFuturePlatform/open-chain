package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.util.NodeTime
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncServerHandler(
        val time : NodeTime
) : BaseHandler(Type.TIME_REQUEST) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        val response = Packet.newBuilder()
                .setType(Type.TIME_RESPONSE)
                .setTimeResponse(TimeResponse.newBuilder()
                        .setTimestamp(time.now())
                        .setInitialTimestamp(message.timeRequest.timestamp)
                        .build())
                .build()
        ctx.channel().writeAndFlush(response)
    }
}
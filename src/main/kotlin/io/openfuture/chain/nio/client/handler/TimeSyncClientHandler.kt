package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.component.NodeClock
import io.openfuture.chain.protocol.CommunicationProtocol.Type.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncClientHandler(
        private val clock: NodeClock
) : BaseHandler(TIME_SYNC_RESPONSE){

    companion object {
        private val log = LoggerFactory.getLogger(TimeSyncClientHandler::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val request = Packet.newBuilder()
                .setType(TIME_SYNC_REQUEST)
                .setTimeSyncRequest(TimeSyncRequest.newBuilder()
                        .setNodeTimestamp(clock.nodeTime())
                        .build())
                .build()
        ctx.writeAndFlush(request)

        log.info("Message $TIME_SYNC_REQUEST was sent to ${ctx.channel().remoteAddress()}")

        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        log.info("Message $TIME_SYNC_RESPONSE received from ${ctx.channel().remoteAddress()}")

        val roundTripTime = clock.nodeTime() - message.timeSyncResponse.nodeTimestamp
        val expectedNetworkTimestamp = message.timeSyncResponse.nodeTimestamp + roundTripTime / 2
        val offset = message.timeSyncResponse.networkTimestamp - expectedNetworkTimestamp

        clock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        clock.removeTimeOffset(ctx.channel().remoteAddress().toString())
        ctx.fireChannelInactive()
    }

}
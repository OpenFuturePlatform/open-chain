package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.TimeSyncService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncClientHandler(
        private val timeSyncService: TimeSyncService
) : BaseHandler(Type.TIME_SYNC_RESPONSE){

    companion object {
        private val log = LoggerFactory.getLogger(TimeSyncClientHandler::class.java)
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        log.info("Time response received from ${ctx.channel().remoteAddress()}")
        timeSyncService.calculateAndAddTimeOffset(message, ctx.channel().remoteAddress().toString())
    }

}
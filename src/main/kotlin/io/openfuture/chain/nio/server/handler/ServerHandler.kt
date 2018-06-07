package io.openfuture.chain.nio.server.handler

import io.netty.channel.*
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.nio.server.service.TimeSynchronizationService
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ServerHandler(
        private val timeSynchronizationService: TimeSynchronizationService
) : SimpleChannelInboundHandler<CommunicationProtocolOuterClass.CommunicationProtocol>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is active")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: CommunicationProtocolOuterClass.CommunicationProtocol) {
        val serviceName = msg.serviceName
        if (timeSynchronizationService.canHandleService(serviceName)) {
            val payload = timeSynchronizationService.takeMessage(msg)
            val serviceResponse = timeSynchronizationService.service(payload)
            val message = timeSynchronizationService.updatePacketByMessage(msg, serviceResponse)
            ctx.channel().writeAndFlush(message)
        }
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
        if (evt is IdleStateEvent) {
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is inactive")
    }

}
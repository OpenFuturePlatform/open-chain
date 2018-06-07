package io.openfuture.chain.nio.server.handler

import io.netty.channel.*
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.message.TimeSynchronization
import io.openfuture.chain.nio.server.service.ProtobufService
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ServerHandler(
        private val services: List<ProtobufService<TimeSynchronization.TimeSynchronizationMessage>>
) : SimpleChannelInboundHandler<CommunicationProtocolOuterClass.CommunicationProtocol>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is active")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: CommunicationProtocolOuterClass.CommunicationProtocol) {
        val service = services.find { it.canHandlePacket(msg.serviceName) }
        if (service != null) {
            ctx.channel().writeAndFlush(service.handleMessage(msg))
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
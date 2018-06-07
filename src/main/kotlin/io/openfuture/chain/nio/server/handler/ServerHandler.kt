package io.openfuture.chain.nio.server.handler

import io.netty.channel.*
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.message.TimeSynchronization
import io.openfuture.chain.nio.client.handler.ClientHandler
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ServerHandler : SimpleChannelInboundHandler<CommunicationProtocolOuterClass.CommunicationProtocol>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Channel ${ctx.channel().remoteAddress()} is active")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: CommunicationProtocolOuterClass.CommunicationProtocol) {
        val serviceName = msg.serviceName
        if (serviceName == CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName.TIME_SYNCHRONIZATION) {
            val payload = TimeSynchronization.TimeSynchronizationMessage.parseFrom(msg.servicePayload)
            log.info("Request was sent at : ${payload.requestTime} milliseconds")

            val newPayload = payload.toBuilder().setResponseTime(System.currentTimeMillis()).build()
            val message = msg.toBuilder().setServicePayload(newPayload.toByteString()).build()

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
package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.message.TimeSynchronization
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName.TIME_SYNCHRONIZATION
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ClientHandler : SimpleChannelInboundHandler<CommunicationProtocolOuterClass.CommunicationProtocol>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext,
                              msg: CommunicationProtocolOuterClass.CommunicationProtocol) {
        val serviceName = msg.serviceName
        if (serviceName == TIME_SYNCHRONIZATION) {
            val payload = TimeSynchronization.TimeSynchronizationMessage.parseFrom(msg.servicePayload)
            log.info("Request time: ${payload.requestTime}, response time: ${payload.responseTime}, difference: " +
                    "${payload.responseTime - payload.requestTime}")
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val servicePayloadBuilder = TimeSynchronization.TimeSynchronizationMessage.newBuilder()
        val servicePayload = servicePayloadBuilder.setRequestTime(System.currentTimeMillis()).build()
        val messageBuilder = CommunicationProtocolOuterClass.CommunicationProtocol.newBuilder()
        val message = messageBuilder
                .setServiceName(TIME_SYNCHRONIZATION)
                .setServicePayload(servicePayload.toByteString())
                .build()

        ctx.channel().writeAndFlush(message)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.message.TimeSynchronization
import io.openfuture.chain.nio.client.service.TimeSynchronizationClient
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass.CommunicationProtocol.ServiceName.TIME_SYNCHRONIZATION
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ClientHandler(
        private val timeSynchronizationClient: TimeSynchronizationClient
) : SimpleChannelInboundHandler<CommunicationProtocolOuterClass.CommunicationProtocol>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext,
                              msg: CommunicationProtocolOuterClass.CommunicationProtocol) {
        val serviceName = msg.serviceName
        if (timeSynchronizationClient.canHandleResponse(serviceName)) {
            timeSynchronizationClient.handleResponse(msg)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
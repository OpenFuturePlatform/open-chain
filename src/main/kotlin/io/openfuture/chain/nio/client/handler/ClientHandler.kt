package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.nio.client.service.ProtobufClient
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
@ChannelHandler.Sharable
class ClientHandler(
        private val clients: List<ProtobufClient<*>>
) : SimpleChannelInboundHandler<CommunicationProtocolOuterClass.CommunicationProtocol>() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext,
                              msg: CommunicationProtocolOuterClass.CommunicationProtocol) {
        val client = clients.find { it.canHandleResponse(msg.serviceName) }
        if (client != null) {
            client.handleResponse(msg)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
package io.openfuture.chain.nio.client.handler

import io.netty.channel.*
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
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
        log.info("Request time: ${msg.requestTime}, response time: ${msg.responseTime}, difference: " +
                "${msg.responseTime - msg.requestTime}")
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val messageBuilder = CommunicationProtocolOuterClass.CommunicationProtocol.newBuilder()
        val message = messageBuilder.setRequestTime(System.currentTimeMillis()).build()
        ctx.channel().writeAndFlush(message)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Hop stop")
    }

}
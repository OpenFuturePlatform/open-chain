package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.component.BlockChain
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.protocol.CommunicationProtocol.HeartBeat.Type.PING
import io.openfuture.chain.protocol.CommunicationProtocol.HeartBeat.Type.PONG
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

@Component
@Scope("prototype")
class HeartBeatServerHandler(
        private val blockChain: BlockChain
) : BaseHandler(Type.HEART_BEAT) {

    companion object {
        private val log = LoggerFactory.getLogger(HeartBeatServerHandler::class.java)
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val body = message.heartBeat
        log.info("Heartbeat ({}) from: {}", body.type, ctx.channel().remoteAddress())

        if (body.type == PING) {
            val recipientKey = ctx.channel().remoteAddress().toString()
            val senderKey = ctx.channel().localAddress().toString()
            blockChain.addTransaction(TransactionRequest(100, Date().time, recipientKey,
                    senderKey, "current_node_signature")) //todo test

            val response = Packet.newBuilder()
                    .setType(Type.HEART_BEAT)
                    .setHeartBeat(HeartBeat.newBuilder().setType(PONG).build())
                    .build()
            ctx.writeAndFlush(response)
        }
    }

}
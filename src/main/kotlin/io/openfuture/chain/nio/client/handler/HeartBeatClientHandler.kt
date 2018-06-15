package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
@Scope("prototype")
class HeartBeatClientHandler : BaseHandler(Type.HEART_BEAT) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    @Volatile
    private var heartBeatTask: ScheduledFuture<*>? = null


    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val body = message.heartBeat
        log.info("Heartbeat ({}) from: {}", body.type, ctx.channel().remoteAddress())

        if (body.type == HeartBeat.Type.PONG) {
            return
        }

        // heartbeat
        heartBeatTask?.cancel(true)
        heartBeatTask = ctx.channel()
                .eventLoop()
                .scheduleAtFixedRate(HeartBeatTask(ctx), 20, 20, TimeUnit.SECONDS)

        // response
        val packet = Packet.newBuilder()
                .setType(Type.HEART_BEAT)
                .setHeartBeat(HeartBeat.newBuilder().setType(HeartBeat.Type.PONG).build())
                .build()
        ctx.writeAndFlush(packet)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        heartBeatTask?.cancel(true)
        ctx.fireChannelInactive()
    }

    class HeartBeatTask(private val ctx: ChannelHandlerContext) : Runnable {

        override fun run() {
            val packet = Packet.newBuilder()
                    .setType(Type.HEART_BEAT)
                    .setHeartBeat(HeartBeat.newBuilder().setType(HeartBeat.Type.PING).build())
                    .build()
            ctx.writeAndFlush(packet)
        }

    }
}
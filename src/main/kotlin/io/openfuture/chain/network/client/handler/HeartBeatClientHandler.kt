package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.HeartBeat
import io.openfuture.chain.network.domain.HeartBeat.Type.*
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
@Scope("prototype")
class HeartBeatClientHandler : BaseHandler<HeartBeat>() {

    @Volatile
    private var heartBeatTask: ScheduledFuture<*>? = null


    override fun packetReceived(ctx: ChannelHandlerContext, message: HeartBeat) {
        if (message.type == PONG) {
            return
        }

        // heartbeat
        heartBeatTask?.cancel(true)
        heartBeatTask = ctx.channel()
            .eventLoop()
            .scheduleAtFixedRate(HeartBeatTask(ctx), 20, 20, TimeUnit.SECONDS)

        // response
        ctx.writeAndFlush(HeartBeat(PONG))
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        heartBeatTask?.cancel(true)
    }

    class HeartBeatTask(private val ctx: ChannelHandlerContext) : Runnable {

        override fun run() {
            ctx.writeAndFlush(HeartBeat(PING))
        }

    }

}
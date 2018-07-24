package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.HeartBeat
import io.openfuture.chain.network.domain.HeartBeat.Type.PING
import io.openfuture.chain.network.domain.HeartBeat.Type.PONG
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.SECONDS

@Component
@Scope(SCOPE_PROTOTYPE)
class HeartBeatClientHandler : ClientHandler<HeartBeat>() {

    private var heartBeatTask: ScheduledFuture<*>? = null


    override fun channelRead0(ctx: ChannelHandlerContext, message: HeartBeat) {
        if (message.type == PONG) {
            return
        }

        heartBeatTask?.cancel(true)
        heartBeatTask = ctx.channel()
                .eventLoop()
                .scheduleAtFixedRate({ ctx.writeAndFlush(HeartBeat(PING)) }, 20, 20, SECONDS)

        ctx.writeAndFlush(HeartBeat(PONG))
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        heartBeatTask?.cancel(true)
    }

}
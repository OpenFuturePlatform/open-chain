package io.openfuture.chain.network.service.message

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class HeartBeatMessageService{

    private val tasks: MutableMap<Channel, ScheduledFuture<*>> = ConcurrentHashMap()

    fun handleChannelActive(ctx: ChannelHandlerContext) {
        val task = ctx.channel()
            .eventLoop()
            .scheduleAtFixedRate({ ctx.writeAndFlush(HeartBeatMessage(PING)) }, 0, 20, TimeUnit.SECONDS)
        tasks[ctx.channel()] = task
    }

    fun handleHeartBeatMessage(ctx: ChannelHandlerContext, heartBeat: HeartBeatMessage) {
        if (heartBeat.type == PING) {
            ctx.channel().writeAndFlush(HeartBeatMessage(PONG))
        }
    }

    fun handleChannelInactive(ctx: ChannelHandlerContext) {
        val task = tasks.remove(ctx.channel())!!
        task.cancel(true)
    }

}
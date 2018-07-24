package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.HeartBeat
import io.openfuture.chain.network.domain.HeartBeat.Type.PING
import io.openfuture.chain.network.domain.HeartBeat.Type.PONG
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class HeartBeatServerHandler : ServerHandler<HeartBeat>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(HeartBeat(PING))
    }

    override fun channelRead0(ctx: ChannelHandlerContext, message: HeartBeat) {
        if (message.type == PING) {
            ctx.writeAndFlush(HeartBeat(PONG))
        }
    }

}
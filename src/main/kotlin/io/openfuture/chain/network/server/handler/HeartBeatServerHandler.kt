package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.base.handler.BaseHandler
import io.openfuture.chain.network.domain.HeartBeat
import io.openfuture.chain.network.domain.HeartBeat.Type.*
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class HeartBeatServerHandler : BaseHandler<HeartBeat>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: HeartBeat) {
        if (message.type == PING) {
            ctx.writeAndFlush(HeartBeat(PONG))
        }
    }

}
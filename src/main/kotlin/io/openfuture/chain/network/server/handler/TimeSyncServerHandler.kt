package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.network.base.handler.BaseHandler
import io.openfuture.chain.network.domain.AskTime
import io.openfuture.chain.network.domain.Time
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncServerHandler(
    private val clock: NodeClock
) : BaseHandler<AskTime>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: AskTime) {
        ctx.channel().writeAndFlush(Time(clock.networkTime(), message.nodeTimestamp))
    }

}
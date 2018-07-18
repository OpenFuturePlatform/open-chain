package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.TimeSyncRequest
import io.openfuture.chain.network.domain.TimeSyncResponse
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncServerHandler(
        private val clock: NodeClock
) : BaseHandler<TimeSyncRequest>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: TimeSyncRequest) {
        ctx.channel().writeAndFlush(TimeSyncResponse(clock.networkTime(), message.nodeTimestamp))
    }

}
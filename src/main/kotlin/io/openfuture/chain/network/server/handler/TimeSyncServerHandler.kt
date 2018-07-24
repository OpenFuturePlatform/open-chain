package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.network.domain.AskTime
import io.openfuture.chain.network.domain.Time
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class TimeSyncServerHandler(
        private val clock: NodeClock
) : ServerHandler<AskTime>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: AskTime) {
        ctx.channel().writeAndFlush(Time(clock.networkTime(), message.nodeTimestamp))
    }

}
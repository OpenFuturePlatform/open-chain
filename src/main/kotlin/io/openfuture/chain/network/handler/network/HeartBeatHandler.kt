package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState.READER_IDLE
import io.netty.handler.timeout.IdleState.WRITER_IDLE
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.network.HeartBeatMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class HeartBeatHandler(
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<HeartBeatMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HeartBeatMessage) {
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, event: Any) {
        if (event !is IdleStateEvent) {
            super.userEventTriggered(ctx, event)
            return
        }

        val eventState = event.state()
        if (READER_IDLE == eventState) {
            channelsHolder.removeChannel(ctx.channel())
        } else if (WRITER_IDLE == eventState) {
            ctx.writeAndFlush(HeartBeatMessage())
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        }
    }

}

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
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import org.springframework.stereotype.Component

@Component
@Sharable
class HeartBeatHandler(
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<HeartBeatMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HeartBeatMessage) {
        if (msg.type == PING) {
            ctx.writeAndFlush(HeartBeatMessage(PONG))
        }
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt !is IdleStateEvent) {
            super.userEventTriggered(ctx, evt)
            return
        }

        when (evt.state()) {
            READER_IDLE -> {
                channelsHolder.removeChannel(ctx.channel())
            }
            WRITER_IDLE -> ctx.writeAndFlush(HeartBeatMessage())
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
            else -> {
            }
        }
    }

}

package io.openfuture.chain.network.handler.network

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

        val a = channelsHolder.getClientsAddresses()
        val b = channelsHolder.getPeersAddresses()
        System.err.println(jacksonObjectMapper().writeValueAsString(a))
        System.err.println()
        System.err.println(jacksonObjectMapper().writeValueAsString(b))
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            when (evt.state()) {
                READER_IDLE -> ctx.close()
                WRITER_IDLE -> ctx.writeAndFlush(HeartBeatMessage())
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
                else -> throw IllegalStateException("Not processing this type")
            }
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }

}

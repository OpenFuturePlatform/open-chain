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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class HeartBeatHandler(
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<HeartBeatMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(HeartBeatHandler::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HeartBeatMessage) {
        if (null != channelsHolder.getNodeInfoByChannelId(ctx.channel().id())) {
            log.info("Received heartbeat message from ${ctx.channel().remoteAddress()}")
        } else {
            log.info("Unknown heartbeat from ${ctx.channel().remoteAddress()}")
        }
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, event: Any) {
        if (event !is IdleStateEvent) {
            super.userEventTriggered(ctx, event)
            return
        }
        //log.info("Idle event inbound to ${ctx.channel().remoteAddress()}")

        val eventState = event.state()
        if (READER_IDLE == eventState) {
            channelsHolder.removeChannel(ctx.channel())
        } else if (WRITER_IDLE == eventState && null != channelsHolder.getNodeInfoByChannelId(ctx.channel().id())) {
            //log.info("Sending heartbeat message to ${ctx.channel().remoteAddress()}")
            ctx.writeAndFlush(HeartBeatMessage())
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        }
    }

}

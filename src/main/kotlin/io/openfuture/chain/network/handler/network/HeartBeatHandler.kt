package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState.READER_IDLE
import io.netty.handler.timeout.IdleState.WRITER_IDLE
import io.netty.handler.timeout.IdleStateEvent
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import org.springframework.stereotype.Component

@Component
@Sharable
class HeartBeatHandler(
    private val channelsHolder: ChannelsHolder,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : SimpleChannelInboundHandler<HeartBeatMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HeartBeatMessage) {
        if (msg.type == PING) {
            ctx.writeAndFlush(HeartBeatMessage(explorerAddressesHolder.getAddresses(), PONG))
        }

        val addresses = msg.explorerAddress.toMutableSet().minus(explorerAddressesHolder.getAddresses())
        explorerAddressesHolder.addAddresses(addresses)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt !is IdleStateEvent) {
            super.userEventTriggered(ctx, evt)
        }

        when ((evt as IdleStateEvent).state()) {
            READER_IDLE -> {
                explorerAddressesHolder.removeAddress(channelsHolder.getAddressByChannelId(ctx.channel().id())!!)
                channelsHolder.removeChannel(ctx.channel())
                ctx.close()
            }
            WRITER_IDLE -> ctx.writeAndFlush(HeartBeatMessage())
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
            else -> {}
        }
    }

}

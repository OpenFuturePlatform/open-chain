package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class ConnectionHandler(
    private val channelsHolder: ChannelsHolder,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : SimpleChannelInboundHandler<Serializable>() {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.debug("Connection with ${ctx.channel().remoteAddress()} established")
        super.channelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Serializable?) {
        ctx.fireChannelRead(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.debug("Connection with ${ctx.channel().remoteAddress()} closed")

        val address = channelsHolder.getAddressByChannelId(ctx.channel().id())
        if (null != address) {
            explorerAddressesHolder.removeAddress(address)
            channelsHolder.removeChannel(ctx.channel())
        }

        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with cause", cause)

        val address = channelsHolder.getAddressByChannelId(ctx.channel().id())
        if (null != address) {
            explorerAddressesHolder.removeAddress(address)
            channelsHolder.removeChannel(ctx.channel())
        }

        ctx.close()
    }

}

package io.openfuture.chain.node.server

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.CharsetUtil


class ServerHandler(val channelHandlerContexts: MutableList<ChannelHandlerContext>) : ChannelInboundHandlerAdapter() {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        channelHandlerContexts.add(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        channelHandlerContexts.remove(ctx)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val inBuffer = msg as ByteBuf
        val received = inBuffer.toString(CharsetUtil.UTF_8)
        for (channelHandlerContext in channelHandlerContexts) {
            if (channelHandlerContext == ctx) {
                continue
            }
            val buffer = channelHandlerContext.alloc().buffer()
            buffer.writeBytes("Hello $received".toByteArray(CharsetUtil.UTF_8))
//            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("Hello $received", CharsetUtil.UTF_8))
            channelHandlerContext.writeAndFlush(buffer)
        }
    }
}
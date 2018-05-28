package io.zensoft.netty.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter


class ServerHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val requestData = msg as String
        ctx.writeAndFlush(requestData + requestData)
        //    future.addListener(ChannelFutureListener.CLOSE);
    }
}
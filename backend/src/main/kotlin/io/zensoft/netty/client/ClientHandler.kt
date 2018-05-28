package io.zensoft.netty.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter


class ClientHandler : ChannelInboundHandlerAdapter() {

//    @Throws(Exception::class)
//    override fun channelActive(ctx: ChannelHandlerContext) {
//        ctx.writeAndFlush("123456");
//    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val requestData = msg as String
        println(requestData)
    //    ctx.close()
    }
}
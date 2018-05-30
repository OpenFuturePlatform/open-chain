package io.openfuture.chain.node.client

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.CharsetUtil
import org.springframework.stereotype.Component


class ClientHandler : SimpleChannelInboundHandler<ByteBuf>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        val message = "Client received: " + msg.toString(CharsetUtil.UTF_8)
        println(message)
    }
}
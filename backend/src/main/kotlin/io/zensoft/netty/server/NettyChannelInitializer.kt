package io.zensoft.netty.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel


class NettyChannelInitializer : ChannelInitializer<SocketChannel>() {

    @Throws(Exception::class)
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(
                RequestDecoder(),
                ResponseDataEncoder(),
                ServerHandler())
    }
}
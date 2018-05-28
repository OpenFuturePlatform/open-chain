package io.zensoft.netty.client

import io.netty.channel.ChannelPipeline
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel


class NettyChannelInitializer : ChannelInitializer<SocketChannel>() {

    @Throws(Exception::class)
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(
                RequestDataEncoder(),
                ResponseDataDecoder(),
                ClientHandler())
    }
}
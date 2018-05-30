package io.openfuture.chain.node.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList


@Component
class NodeServerChannelInitializer : ChannelInitializer<SocketChannel>() {

    var channelHandlerContexts: MutableList<ChannelHandlerContext> = CopyOnWriteArrayList();

    @Throws(Exception::class)
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(ServerHandler(channelHandlerContexts))
    }
}
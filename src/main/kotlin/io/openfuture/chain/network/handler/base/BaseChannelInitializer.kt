package io.openfuture.chain.network.handler.base

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.ApplicationContext

abstract class BaseChannelInitializer(
    private val connectionHandler: BaseConnectionHandler,
    private val context: ApplicationContext
) : ChannelInitializer<SocketChannel>() {

    final override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(context.getBean(PacketDecoder::class.java))
        pipeline.addLast(context.getBean(PacketEncoder::class.java))

        pipeline.addLast(ReadTimeoutHandler(60))
        pipeline.addLast(connectionHandler)
    }

}
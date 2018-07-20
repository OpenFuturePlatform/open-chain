package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.openfuture.chain.network.base.PacketDecoder
import io.openfuture.chain.network.base.PacketEncoder

abstract class BaseChannelInitializer(
        private val decoder: PacketDecoder,
        private val encoder: PacketEncoder,
        private val connectionHandler: BaseConnectionHandler,
        private val commonHandlers: Array<CommonHandler<*>>
) : ChannelInitializer<SocketChannel>() {

    final override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(decoder)
        pipeline.addLast(encoder)

        pipeline.addLast(ReadTimeoutHandler(60))
        pipeline.addLast(connectionHandler)
        pipeline.addLast(*commonHandlers)
    }

    abstract fun initChannel(pipeline: ChannelPipeline)

}
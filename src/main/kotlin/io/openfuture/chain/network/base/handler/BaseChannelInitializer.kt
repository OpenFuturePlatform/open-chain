package io.openfuture.chain.network.base.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import io.openfuture.chain.network.base.PacketDecoder
import io.openfuture.chain.network.base.PacketEncoder
import org.springframework.context.ApplicationContext

abstract class BaseChannelInitializer(
    protected val context: ApplicationContext,
    private val connectionHandlerClass: Class<out BaseConnectionHandler>
) : ChannelInitializer<SocketChannel>() {

    final override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(context.getBean(PacketDecoder::class.java))
        pipeline.addLast(context.getBean(PacketEncoder::class.java))

        pipeline.addLast(ReadTimeoutHandler(60))
        pipeline.addLast(context.getBean(connectionHandlerClass))
        pipeline.addLast(context.getBean(GreetingHandler::class.java))
        pipeline.addLast(context.getBean(AddressDiscoveryHandler::class.java))
        pipeline.addLast(context.getBean(AddressHandler::class.java))

        initChannel(pipeline)
    }

    abstract fun initChannel(pipeline: ChannelPipeline)

}
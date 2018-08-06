package io.openfuture.chain.network.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

class ChannelInitializer(
    private val connectionHandlerClass: KClass<out BaseConnectionHandler>,
    private val context: ApplicationContext
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(context.getBean(PacketDecoder::class.java))
        pipeline.addLast(context.getBean(PacketEncoder::class.java))
        pipeline.addLast(ReadTimeoutHandler(60))
        pipeline.addLast(context.getBean(connectionHandlerClass.java))
    }

}
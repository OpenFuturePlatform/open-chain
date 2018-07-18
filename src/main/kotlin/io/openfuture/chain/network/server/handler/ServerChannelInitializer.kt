package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.openfuture.chain.network.base.*
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
    private val context: ApplicationContext
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast(context.getBean(PacketDecoder::class.java))
        pipeline.addLast(context.getBean(PacketEncoder::class.java))

        // Handlers
        pipeline.addLast(ReadTimeoutHandler(60))
        pipeline.addLast(context.getBean(ConnectionServerHandler::class.java))
        pipeline.addLast(context.getBean(GreetingHandler::class.java))
        pipeline.addLast(context.getBean(TimeSyncServerHandler::class.java))
        pipeline.addLast(context.getBean(AddressDiscoveryHandler::class.java))
        pipeline.addLast(context.getBean(AddressHandler::class.java))
        pipeline.addLast(context.getBean(HeartBeatServerHandler::class.java)) // prototype
    }

}
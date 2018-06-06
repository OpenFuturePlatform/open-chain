package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.openfuture.chain.property.NodeProperties
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
class ServerChannelInitializer(
    private val serverHandler : ChannelInboundHandlerAdapter,
    private val properties: NodeProperties
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val idleStateHandler = IdleStateHandler(0, 0, properties.pingTime!!)
        channel.pipeline().addLast(idleStateHandler, ProtobufEncoder(), StringEncoder(), serverHandler)
    }

}
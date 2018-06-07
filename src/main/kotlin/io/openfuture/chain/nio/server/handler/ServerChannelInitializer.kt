package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
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
        val protocol = CommunicationProtocolOuterClass.CommunicationProtocol.getDefaultInstance()
        channel.pipeline()
                .addLast(idleStateHandler)
                .addLast(ProtobufDecoder(protocol))
                .addLast(ProtobufEncoder())
                .addLast(serverHandler)
    }

}
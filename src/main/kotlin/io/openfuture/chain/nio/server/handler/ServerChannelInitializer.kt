package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
class ServerChannelInitializer(
    private val timeHandler: TimeRequestHandler,
    private val properties: NodeProperties
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val idleStateHandler = IdleStateHandler(0, 0, properties.pingTime!!)
        val timeRequest = CommunicationProtocol.TimeRequest.getDefaultInstance()
        channel.pipeline()
                .addLast(idleStateHandler)
                .addLast(ProtobufDecoder(timeRequest))
                .addLast(ProtobufEncoder())
                .addLast(timeHandler)
    }

}
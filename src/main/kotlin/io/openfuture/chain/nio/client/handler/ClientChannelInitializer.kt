package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.openfuture.chain.protocol.CommunicationProtocolOuterClass
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
class ClientChannelInitializer(
    private val clientHandler: ClientHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val protocol = CommunicationProtocolOuterClass.CommunicationProtocol.getDefaultInstance()
        ch.pipeline()
                .addLast(ProtobufDecoder(protocol))
                .addLast(ProtobufEncoder())
                .addLast(clientHandler)
    }

}
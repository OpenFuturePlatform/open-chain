package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.openfuture.chain.message.TimeMessageProtos
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
class ClientChannelInitializer(
    private val clientHandler: ClientHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
                .addLast(ProtobufDecoder(TimeMessageProtos.TimeMessage.getDefaultInstance()))
                .addLast(ProtobufEncoder())
                .addLast(clientHandler)
    }

}
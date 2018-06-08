package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
class ClientChannelInitializer(
    private val timeHandler: TimeResponseHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val timeResponse = CommunicationProtocol.TimeResponse.getDefaultInstance()
        ch.pipeline()
                .addLast(ProtobufDecoder(timeResponse))
                .addLast(ProtobufEncoder())
                .addLast(timeHandler)
    }

}
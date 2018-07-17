package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.timeout.ReadTimeoutHandler
import io.openfuture.chain.network.base.GreetingHandler
import io.openfuture.chain.network.base.AddressHandler
import io.openfuture.chain.network.base.AddressDiscoveryHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class ServerChannelInitializer(
    private val context: ApplicationContext
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        // Decoders
        pipeline.addLast(ProtobufVarint32FrameDecoder())
        pipeline.addLast(ProtobufDecoder(CommunicationProtocol.Packet.getDefaultInstance()))

        // Encoders
        pipeline.addLast(ProtobufVarint32LengthFieldPrepender())
        pipeline.addLast(ProtobufEncoder())

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
package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.property.NodeProperty
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.NetworkService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GreetingHandler(
    private val networkService: NetworkService,
    private val properties: NodeProperty
) : BaseHandler(Type.GREETING) {

    override fun channelActive(ctx: ChannelHandlerContext) {
        val message = Packet.newBuilder()
            .setType(Type.GREETING)
            .setGreeting(Greeting.newBuilder()
                .setAddress(CommunicationProtocol.NetworkAddress.newBuilder()
                    .setHost(properties.host)
                    .setPort(properties.port!!))
                .build())
            .build()
        ctx.writeAndFlush(message)

        ctx.fireChannelActive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val addressMessage = message.greeting.address
        val address = NetworkAddress(addressMessage.host, addressMessage.port)
        networkService.addConnection(ctx.channel(), address)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        networkService.removeConnection(ctx.channel())
        ctx.fireChannelInactive()
    }

}
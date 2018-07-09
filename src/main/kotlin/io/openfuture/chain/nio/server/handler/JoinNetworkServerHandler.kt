package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.NetworkAddress
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.NetworkAddressService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Scope("prototype")
class JoinNetworkServerHandler(
    private val addressService: NetworkAddressService,
    private val networkService: NetworkService
) : BaseHandler(Type.JOIN_NETWORK_REQUEST) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val clientHost = (ctx.channel().remoteAddress() as InetSocketAddress).hostName
        val clientPort = message.joinNetworkRequest.port
        val clientId = clientHost + clientPort
        addressService.save(NetworkAddress(clientId, clientHost, clientPort))

        networkService.broadcast(createMessageToAddNetworkAddress(clientId, clientHost, clientPort))

        val response = Packet.newBuilder()
            .setType(Type.JOIN_NETWORK_RESPONSE)
            .setJoinNetworkResponse(JoinNetworkResponse.newBuilder()
                .addAllNetworkAddresses(addressService.findAll())
                .setHost(clientHost)
                .setNodeId(clientId)
                .build())
            .build()
        ctx.writeAndFlush(response)
    }

    private fun createMessageToAddNetworkAddress(id : String, host: String, port: Int)
        : CommunicationProtocol.Packet {
        return Packet.newBuilder()
            .setType(Type.UPDATE_NETWORK_ADDRESSES)
            .setUpdateNetworkAddresses(UpdateNetworkAddresses.newBuilder()
                .setType(UpdateNetworkAddresses.Type.ADD)
                .setAddAddress(UpdateNetworkAddresses.AddAddress.newBuilder()
                    .setAddress(CommunicationProtocol.NetworkAddress.newBuilder()
                        .setNodeId(id)
                        .setHost(host)
                        .setPort(port)
                        .build())
                    .build()))
            .build()
    }

}
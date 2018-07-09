package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Node
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.NodeService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Scope("prototype")
class JoinNetworkServerHandler(
    private val nodeService: NodeService,
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.JOIN_NETWORK_REQUEST) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val clientHost = (ctx.channel().remoteAddress() as InetSocketAddress).hostName
        val clientPort = message.joinNetworkRequest.port
        val clientId = clientHost + clientPort
        nodeService.save(Node(clientId, clientHost, clientPort))

        networkService.broadcast(createRoutingTableMessage(clientId, clientHost, clientPort))

        val response = CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.JOIN_NETWORK_RESPONSE)
            .setJoinNetworkResponse(CommunicationProtocol.JoinNetworkResponse.newBuilder()
                .addAllNodes(nodeService.findAll())
                .setHost(clientHost)
                .setNetworkId(clientId)
                .build())
            .build()
        ctx.writeAndFlush(response)
    }

    private fun createRoutingTableMessage(id : String, host: String, port: Int) : CommunicationProtocol.Packet {
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.UPDATE_ROUTING_TABLE)
            .setUpdateRoutingTable(CommunicationProtocol.UpdateRoutingTable.newBuilder()
                .setType(CommunicationProtocol.UpdateRoutingTable.Type.ADD)
                .setAddRow(CommunicationProtocol.UpdateRoutingTable.AddRow.newBuilder()
                    .setNode(CommunicationProtocol.Node.newBuilder()
                        .setNetworkId(id)
                        .setHost(host)
                        .setPort(port)
                        .build())
                    .build()))
            .build()
    }


}
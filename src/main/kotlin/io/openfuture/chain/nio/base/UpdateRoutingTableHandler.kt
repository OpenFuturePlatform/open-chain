package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Node
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.NodeService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class UpdateRoutingTableHandler(
    private val nodeService: NodeService,
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.UPDATE_ROUTING_TABLE) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        val payload = message.updateRoutingTable
        if (payload.type == CommunicationProtocol.UpdateRoutingTable.Type.ADD) {
            val newNode = payload.addRow.node
            val existingNode = nodeService.findByNetworkId(newNode.networkId)
            if (existingNode == null) {
                nodeService.save(Node(newNode.networkId, newNode.host, newNode.port))
                networkService.broadcast(message)
            }
        }

        if (payload.type == CommunicationProtocol.UpdateRoutingTable.Type.REMOVE) {
            val id = message.updateRoutingTable.removeRow.networkId
            val existingNode = nodeService.findByNetworkId(id)
            if (existingNode != null) {
                nodeService.deleteByNetworkId(id)
                networkService.broadcast(message)
            }
        }
    }
}
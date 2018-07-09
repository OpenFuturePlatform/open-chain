package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.ChannelAttributes
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.NodeService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class DisconnectHandler(
    private val nodeService: NodeService,
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.DISCONNECT){

    private var leaveNetwork = true

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val id = ctx.channel().attr(ChannelAttributes.REMOTE_NODE_ID).get()
        if (leaveNetwork && id != null) {
            nodeService.deleteByNetworkId(id)
            networkService.broadcast(createRoutingTableMessage(id))
        }
        ctx.fireChannelInactive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        leaveNetwork = message.disconnect.leaveNetwork
    }

    private fun createRoutingTableMessage(id : String) : CommunicationProtocol.Packet {
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.UPDATE_ROUTING_TABLE)
            .setUpdateRoutingTable(CommunicationProtocol.UpdateRoutingTable.newBuilder()
                .setType(CommunicationProtocol.UpdateRoutingTable.Type.REMOVE)
                .setRemoveRow(CommunicationProtocol.UpdateRoutingTable.RemoveRow.newBuilder()
                    .setNetworkId(id)
                    .build()))
            .build()
    }
}
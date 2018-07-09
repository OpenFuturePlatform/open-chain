package io.openfuture.chain.nio.base

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.ChannelAttributes
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.service.NetworkService
import io.openfuture.chain.service.NetworkAddressService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class DisconnectHandler(
    private val addressService: NetworkAddressService,
    private val networkService: NetworkService
) : BaseHandler(CommunicationProtocol.Type.DISCONNECT){

    private var leaveNetwork = true

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val nodeId = ctx.channel().attr(ChannelAttributes.REMOTE_NODE_ID).get()
        if (leaveNetwork && nodeId != null) {
            addressService.deleteByNodeId(nodeId)
            networkService.broadcast(createMessageToRemoveNetworkAddress(nodeId))
        }
        ctx.fireChannelInactive()
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet) {
        leaveNetwork = message.disconnect.leaveNetwork
    }

    private fun createMessageToRemoveNetworkAddress(id : String) : CommunicationProtocol.Packet {
        return CommunicationProtocol.Packet.newBuilder()
            .setType(CommunicationProtocol.Type.UPDATE_NETWORK_ADDRESSES)
            .setUpdateNetworkAddresses(CommunicationProtocol.UpdateNetworkAddresses.newBuilder()
                .setType(CommunicationProtocol.UpdateNetworkAddresses.Type.REMOVE)
                .setRemoveAddress(CommunicationProtocol.UpdateNetworkAddresses.RemoveAddress.newBuilder()
                    .setNodeId(id)
                    .build()))
            .build()
    }
}
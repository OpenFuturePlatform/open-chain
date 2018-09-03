package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.network.ResponsePeersMessage
import io.openfuture.chain.network.service.ConnectionService
import org.springframework.stereotype.Component

@Component
@Sharable
class ResponsePeersHandler(
    private val channelsHolder: ChannelsHolder,
    private val connectionService: ConnectionService
) : SimpleChannelInboundHandler<ResponsePeersMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ResponsePeersMessage) {
        val peers = msg.peers.toMutableList()
        peers.removeAll(channelsHolder.getAllAddresses())
        peers.forEach { connectionService.connect(it) }
    }

}
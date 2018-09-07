package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.sync.DelegateRequestMessage
import io.openfuture.chain.network.message.sync.DelegateResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class DelegateRequestHandler(
    private val genesisBlockService: GenesisBlockService
) : SimpleChannelInboundHandler<DelegateRequestMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DelegateRequestMessage) {
        val delegates = genesisBlockService.getLast().payload.activeDelegates
        val nodesInfo = delegates.map { NodeInfo(it.nodeId, NetworkAddress(it.host, it.port)) }

        ctx.writeAndFlush(DelegateResponseMessage(nodesInfo, msg.synchronizationSessionId))
    }

}
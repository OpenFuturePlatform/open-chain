package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockAvailabilityResponseHandler(
    private val chainSynchronizer: ChainSynchronizer
) : SimpleChannelInboundHandler<BlockAvailabilityResponse>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockAvailabilityResponse) {
        chainSynchronizer.onBlockAvailabilityResponse(msg)
    }

}
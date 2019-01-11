package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.ConflictedBlockResolver
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockAvailabilityResponseHandler(
    private val conflictedBlockResolver: ConflictedBlockResolver
) : SimpleChannelInboundHandler<BlockAvailabilityResponse>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockAvailabilityResponse) {
        conflictedBlockResolver.onBlockAvailabilityResponse(msg)
    }

}
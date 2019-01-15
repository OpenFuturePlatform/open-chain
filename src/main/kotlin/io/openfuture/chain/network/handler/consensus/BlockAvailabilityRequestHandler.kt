package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockAvailabilityRequestHandler(
    private val blockService: BlockService
) : SimpleChannelInboundHandler<BlockAvailabilityRequest>() {

        override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockAvailabilityRequest) {
            val block = blockService.findByHash(msg.hash)
            val height = block?.height ?: -1
            ctx.writeAndFlush(BlockAvailabilityResponse(msg.hash, height))
        }

    }
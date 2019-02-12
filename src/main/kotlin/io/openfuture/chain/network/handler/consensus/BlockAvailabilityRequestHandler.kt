package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockAvailabilityRequestHandler(
    private val blockManager: BlockManager
) : SimpleChannelInboundHandler<BlockAvailabilityRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockAvailabilityRequest) {
        val block = blockManager.findByHash(msg.hash)
        val height = block?.height ?: -1
        val response = if (null != block) {
            val lastGenesisBlock = blockManager.getLastGenesisBlock().toMessage()
            BlockAvailabilityResponse(msg.hash, height, lastGenesisBlock)
        } else {
            BlockAvailabilityResponse(msg.hash, height)
        }
        ctx.writeAndFlush(response)
    }

}
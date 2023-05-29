package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.BlockProductionScheduler
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockAvailabilityRequestHandler(
    private val blockManager: BlockManager
) : SimpleChannelInboundHandler<BlockAvailabilityRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockAvailabilityRequest) {
        println("Received message for BlockAvailabilityRequestHandler: ${msg.hash}")
        log.info("Received message for BlockAvailabilityRequestHandler: ${msg.hash}")
        val block = blockManager.findByHash(msg.hash)
        val height = block?.height ?: -1
        val response = if (null != block) {
            val lastGenesisBlock = blockManager.getLastGenesisBlock().toMessage()
            BlockAvailabilityResponse(msg.hash, height, lastGenesisBlock)
        } else {
            BlockAvailabilityResponse(msg.hash, height)
        }
        log.info("Generated response: ${response.hash}, ${response.genesisBlock}, ${response.height}")
        ctx.writeAndFlush(response)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockAvailabilityRequestHandler::class.java)
    }

}
package io.openfuture.chain.network.service.message

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.network.domain.application.block.BlockRequestMessage
import io.openfuture.chain.network.domain.application.block.GenesisBlockMessage
import io.openfuture.chain.network.domain.application.block.MainBlockMessage
import io.openfuture.chain.service.CommonBlockService
import io.openfuture.chain.service.GenesisBlockService
import io.openfuture.chain.service.MainBlockService
import org.springframework.stereotype.Component

@Component
class BlockMessageService(
    private val blockService: CommonBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService
) {

    fun handleNetworkBlockRequest(ctx: ChannelHandlerContext, request: BlockRequestMessage) {

        val blocks = blockService.getBlocksAfterCurrentHash(request.hash)

        blocks?.forEach {
            when (it) {
                is MainBlock -> ctx.channel().writeAndFlush(MainBlockMessage(it))

                is GenesisBlock -> ctx.channel().writeAndFlush(GenesisBlockMessage(it))
            }
        }

    }

    fun handleNetworkGenesisBlock(ctx: ChannelHandlerContext, block: GenesisBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        genesisBlockService.add(block)
    }

    fun handleNetworkMainBlock(ctx: ChannelHandlerContext, block: MainBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        mainBlockService.add(block)
    }

}


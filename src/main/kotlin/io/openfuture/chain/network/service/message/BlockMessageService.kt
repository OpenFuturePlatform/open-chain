package io.openfuture.chain.network.service.message

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.message.application.block.BlockRequestMessage
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import org.springframework.stereotype.Component

@Component
class BlockMessageService(
    private val blockService:CommonBlockService, // TODO: ask for interface
    private val genesisBlockService: GenesisBlockService, // TODO: ask for interface
    private val mainBlockService: MainBlockService // TODO: ask for interface
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

        //genesisBlockService.add(block) TODO: ask for interface
    }

    fun handleNetworkMainBlock(ctx: ChannelHandlerContext, block: MainBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        //mainBlockService.add(block) TODO: ask for interface
    }

}


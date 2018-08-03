package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import io.openfuture.chain.network.message.application.block.SyncBlockRequestMessage
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import org.springframework.stereotype.Component

@Component
class DefaultCoreMessageService(
    private val blockService: CommonBlockService, // TODO: ask for interface
    private val genesisBlockService: GenesisBlockService, // TODO: ask for interface
    private val mainBlockService: MainBlockService // TODO: ask for interface
) : CoreMessageService {

    override fun onNetworkBlockRequest(ctx: ChannelHandlerContext, request: SyncBlockRequestMessage) {
        val blocks = blockService.getBlocksAfterCurrentHash(request.hash)

        blocks?.forEach {
            /*when (it) {
                is MainBlock -> ctx.channel().writeAndFlush(it.to)

                is GenesisBlock -> ctx.channel().writeAndFlush(GenesisBlockMessage(it))
            }*/
        }
    }

    override fun onGenesisBlock(ctx: ChannelHandlerContext, block: GenesisBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        //genesisBlockService.add(block) TODO: ask for interface
    }

    override fun onMainBlock(ctx: ChannelHandlerContext, block: MainBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        //mainBlockService.add(block) TODO: ask for interface
    }

    override fun onTransferTransaction(ctx: ChannelHandlerContext, tx: TransferTransactionMessage) {}

    override fun onDelegateTransaction(ctx: ChannelHandlerContext, tx: DelegateTransactionMessage) {}

    override fun onVoteTransaction(ctx: ChannelHandlerContext, tx: VoteTransactionMessage) {}

}
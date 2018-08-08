package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.*
import org.springframework.stereotype.Service

// TODO: call core API here
@Service
class DefaultCoreMessageService(
    private val blockService: BlockService,
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService
) : CoreMessageService {

    override fun onNetworkBlockRequest(ctx: ChannelHandlerContext, request: SyncBlockRequestMessage) {
//        val blocks = blockService.getBlocksAfterCurrentHash(request.hash)
//
//        blocks.forEach {
//            /*when (it) {
//                    is MainBlock -> ctx.channel().writeAndFlush(it.to)
//
//                    is GenesisBlock -> ctx.channel().writeAndFlush(GenesisBlockMessage(it))
//                }*/
//        }
    }

    override fun onGenesisBlock(ctx: ChannelHandlerContext, block: GenesisBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        //genesisBlockService.save(block)
    }

    override fun onMainBlock(ctx: ChannelHandlerContext, block: MainBlockMessage) {
        if (blockService.isExists(block.hash)) {
            return
        }

        //mainBlockService.save(block)
    }

    override fun onTransferTransaction(ctx: ChannelHandlerContext, tx: TransferTransactionMessage) {
        transferTransactionService.add(tx)
    }

    override fun onDelegateTransaction(ctx: ChannelHandlerContext, tx: DelegateTransactionMessage) {
        delegateTransactionService.add(tx)
    }

    override fun onVoteTransaction(ctx: ChannelHandlerContext, tx: VoteTransactionMessage) {
        voteTransactionService.add(tx)
    }

}
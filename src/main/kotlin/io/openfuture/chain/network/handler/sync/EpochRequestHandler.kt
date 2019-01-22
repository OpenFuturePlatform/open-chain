package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class EpochRequestHandler(
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService,
    private val keyHolder: NodeKeyHolder
) : SimpleChannelInboundHandler<EpochRequestMessage>() {


    override fun channelRead0(ctx: ChannelHandlerContext, msg: EpochRequestMessage) {
        val delegateKey: String = keyHolder.getPublicKeyAsHexString()
        val epochIndex = msg.epochIndex
        val genesisBlock = genesisBlockService.getByEpochIndex(epochIndex)

        if (null == genesisBlock) {
            ctx.writeAndFlush(EpochResponseMessage(delegateKey, false, null, emptyList()))
            return
        }

        val mainBlocks = mainBlockService.getBlocksByEpochIndex(epochIndex)

        if (msg.syncMode != SyncMode.FULL) {
            mainBlocks.forEach {
                it.payload.transferTransactions = mutableListOf()
                it.payload.delegateTransactions = mutableListOf()
                it.payload.voteTransactions = mutableListOf()
            }
        }
        val mainBlockMessages = mainBlocks.map { it.toMessage() }

        ctx.writeAndFlush(EpochResponseMessage(delegateKey, true, genesisBlock.toMessage(), mainBlockMessages))
    }

}
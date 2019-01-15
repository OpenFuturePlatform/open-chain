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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class EpochRequestHandler(
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService,
    private val keyHolder: NodeKeyHolder
) : SimpleChannelInboundHandler<EpochRequestMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EpochRequestHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: EpochRequestMessage) {
        val nodeId: String = keyHolder.getUid()
        val epochIndex = msg.epochIndex
        val genesisBlock = genesisBlockService.getByEpochIndex(epochIndex)

        if (null == genesisBlock) {
            ctx.writeAndFlush(EpochResponseMessage(nodeId, false, null, emptyList()))
            return
        }

        val mainBlockMassages = mainBlockService.getBlocksByEpochIndex(epochIndex)
            .map { it.toMessage() }

        if (msg.syncMode != SyncMode.FULL) {
            mainBlockMassages.forEach {
                it.delegateTransactions = emptyList()
                it.transferTransactions = emptyList()
                it.voteTransactions = emptyList()
            }
        }
        ctx.writeAndFlush(EpochResponseMessage(nodeId, true, genesisBlock.toMessage(), mainBlockMassages))
        log.debug("Send EpochResponseMessage to ${ctx.channel().remoteAddress()}")
    }

}
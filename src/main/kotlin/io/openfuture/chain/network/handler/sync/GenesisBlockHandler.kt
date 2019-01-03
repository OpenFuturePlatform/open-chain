package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.exception.ChainOutOfSyncException
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class GenesisBlockHandler(
    private val genesisBlockService: GenesisBlockService,
    private val chainSynchronizer: ChainSynchronizer
) : SimpleChannelInboundHandler<GenesisBlockMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GenesisBlockHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: GenesisBlockMessage) {
        log.debug("GenesisBlockHandler: ${msg.height}-${msg.hash} from ${ctx.channel().remoteAddress()}")
        try {
            genesisBlockService.add(msg)
        } catch (ex: ChainOutOfSyncException) {
            chainSynchronizer.outOfSync(msg.publicKey)
        }
    }

}
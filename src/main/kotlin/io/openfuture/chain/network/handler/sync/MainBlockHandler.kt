package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.exception.ChainOutOfSyncException
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.network.message.sync.MainBlockMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class MainBlockHandler(
    private val mainBlockService: MainBlockService,
    private val chainSynchronizer: ChainSynchronizer
) : SimpleChannelInboundHandler<MainBlockMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MainBlockHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: MainBlockMessage) {
        log.debug("MainBlockHandler: ${msg.height}-${msg.hash} from ${ctx.channel().remoteAddress()}")
        try {
            mainBlockService.add(msg)
        } catch (e: ChainOutOfSyncException) {
            chainSynchronizer.sync()
        }
    }

}
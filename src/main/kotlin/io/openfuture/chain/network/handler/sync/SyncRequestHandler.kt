package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.sync.SyncBlockDto
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncRequestHandler(
    private val clock: Clock,
    private val blockService: BlockService,
    private val properties: NodeProperties
) : SimpleChannelInboundHandler<SyncRequestMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncRequestHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncRequestMessage) {
        if (properties.expiry!! < clock.currentTimeMillis() - msg.timestamp) {
            log.debug("Expired sync request")
            return
        }

        if (!blockService.isExists(msg.lastBlockHash, msg.lastBlockHeight)) {
            log.debug("Block request NOT EXIST")
            return
        }

        val lastBlocks = blockService.getAfterCurrentHashAndLast30Blocks(msg.lastBlockHash).map { SyncBlockDto(it.height, it.hash) }
//        val blocksAfter = blockService.getAfterCurrentHash(msg.lastBlockHash).map { SyncBlockDto(it.height, it.hash) }
        ctx.writeAndFlush(SyncResponseMessage(clock.currentTimeMillis(), lastBlocks))
        log.debug("REQUEST from ${ctx.channel().remoteAddress()} handled: send ${lastBlocks.size}-[${lastBlocks.map { it.height }}]")
//        ctx.channel().close()
    }

}
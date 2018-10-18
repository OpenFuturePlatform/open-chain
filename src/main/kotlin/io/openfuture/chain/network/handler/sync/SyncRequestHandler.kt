package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncRequestHandler(
    private val clock: Clock,
    private val blockService: BlockService,
    private val properties: NodeProperties
) : SimpleChannelInboundHandler<SyncRequestMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncRequestMessage) {
        if (properties.syncResponseDelay!! < clock.currentTimeMillis() - msg.timestamp) {
            return
        }

        if (!blockService.isExists(msg.lastBlockHash, msg.lastBlockHeight)) {
            return
        }

        val lastBlock = blockService.getLast()
        ctx.writeAndFlush(SyncResponseMessage(clock.currentTimeMillis(), lastBlock.hash, lastBlock.height))
    }

}
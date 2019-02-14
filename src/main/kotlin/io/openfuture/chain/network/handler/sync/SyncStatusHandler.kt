package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.message.base.Message
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncStatusHandler(
    private val chainSynchronizer: ChainSynchronizer
) : SimpleChannelInboundHandler<Message>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Message) {
        if (SYNCHRONIZED == chainSynchronizer.getStatus()) {
            ctx.fireChannelRead(msg)
        }
    }

}
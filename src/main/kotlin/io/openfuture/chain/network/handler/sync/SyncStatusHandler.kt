package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncState
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncStatusHandler(
    private val syncStatus: SyncState
) : SimpleChannelInboundHandler<Serializable>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Serializable) {
        if (syncStatus.getChainStatus() == SyncState.SyncStatusType.SYNCHRONIZED) {
            ctx.fireChannelRead(msg)
        }
    }

}
package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class GenesisBlockHandler(
    private val syncManager: SyncManager
) : SimpleChannelInboundHandler<GenesisBlockMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GenesisBlockMessage) {
        syncManager.onGenesisBlockMessage(msg)
    }

}
package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.sync.HashBlockResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class HashBlockResponseHandler(
    private val syncManager: SyncManager,
    private val channelsHolder: ChannelsHolder
) : SimpleChannelInboundHandler<HashBlockResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HashBlockResponseMessage) {
        syncManager.onHashResponseMessage(msg, channelsHolder.getAddressByChannelId(ctx.channel().id()))
    }

}
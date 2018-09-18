package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.message.sync.DelegateResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class DelegateResponseHandler(
    private val syncManager: SyncManager
) : SimpleChannelInboundHandler<DelegateResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DelegateResponseMessage) {
        syncManager.onDelegateResponseMessage(msg)
    }

}
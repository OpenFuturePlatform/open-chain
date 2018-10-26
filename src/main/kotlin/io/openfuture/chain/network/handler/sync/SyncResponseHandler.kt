package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncResponseHandler(
    private val clock: Clock,
    private val syncManager: SyncManager,
    private val properties: NodeProperties
) : SimpleChannelInboundHandler<SyncResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncResponseMessage) {
        if (properties.syncResponseDelay!! < clock.currentTimeMillis() - msg.timestamp) {
            return
        }
        syncManager.onSyncResponseMessage(msg)
        ctx.close()
    }

}
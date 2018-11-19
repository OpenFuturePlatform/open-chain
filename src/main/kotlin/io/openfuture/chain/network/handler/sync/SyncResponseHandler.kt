package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncResponseHandler(
    private val clock: Clock,
    private val channelsHolder: ChannelsHolder,
    private val syncManager: SyncManager,
    private val properties: NodeProperties
) : SimpleChannelInboundHandler<SyncResponseMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncResponseHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncResponseMessage) {
        if (properties.expiry!! < clock.currentTimeMillis() - msg.timestamp) {
            log.debug("Expired sync response")
            return
        }

        log.debug("RESPONSE from ${ctx.channel().remoteAddress()}")
        syncManager.onSyncResponseMessage(msg, channelsHolder.getNodeInfoByChannelId(ctx.channel().id()))
//        ctx.channel().close()
    }

}
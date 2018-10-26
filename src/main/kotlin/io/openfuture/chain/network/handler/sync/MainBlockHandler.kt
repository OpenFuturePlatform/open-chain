package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.message.sync.MainBlockMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class MainBlockHandler(
    private val mainBlockService: MainBlockService
) : SimpleChannelInboundHandler<MainBlockMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MainBlockMessage) {
        mainBlockService.add(msg)
        ctx.channel().close()
    }

}
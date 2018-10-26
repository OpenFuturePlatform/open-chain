package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class GenesisBlockHandler(
    private val genesisBlockService: GenesisBlockService
) : SimpleChannelInboundHandler<GenesisBlockMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GenesisBlockMessage) {
        genesisBlockService.add(msg)
        ctx.channel().close()
    }

}
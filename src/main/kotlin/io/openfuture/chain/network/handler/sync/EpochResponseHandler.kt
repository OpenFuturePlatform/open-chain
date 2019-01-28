package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class EpochResponseHandler(
    private val chainSynchronizer: ChainSynchronizer
) : SimpleChannelInboundHandler<EpochResponseMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: EpochResponseMessage) {
        chainSynchronizer.onEpochResponse(msg)
    }

}
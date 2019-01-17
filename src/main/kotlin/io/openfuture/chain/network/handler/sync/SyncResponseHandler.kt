package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncResponseHandler(
    private val chainSynchronizer: ChainSynchronizer
) : SimpleChannelInboundHandler<SyncResponseMessage>() {


    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncResponseMessage) {
        chainSynchronizer.onGenesisBlockResponse(msg.genesisBlockMessage)
    }

}
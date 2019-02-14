package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class GenesisBlockHandler(
    private val blockManager: BlockManager
) : SimpleChannelInboundHandler<GenesisBlockMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GenesisBlockMessage) {
        blockManager.add(GenesisBlock.of(msg))
    }

}
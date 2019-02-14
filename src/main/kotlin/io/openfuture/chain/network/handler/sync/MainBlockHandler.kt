package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.network.message.sync.MainBlockMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class MainBlockHandler(
    private val blockManager: BlockManager
) : SimpleChannelInboundHandler<MainBlockMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: MainBlockMessage) {
        blockManager.add(MainBlock.of(msg))
    }

}
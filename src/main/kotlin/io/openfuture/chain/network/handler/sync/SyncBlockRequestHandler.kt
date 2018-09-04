package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.message.sync.SyncBlockRequestMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncBlockRequestHandler(
    private val blockService: BlockService
) : SimpleChannelInboundHandler<SyncBlockRequestMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncBlockRequestMessage) {
        blockService.getAfterCurrentHash(msg.hash)
            .map { it.toMessage() }
            .forEach { response -> ctx.writeAndFlush(response) }
    }

}
package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.message.sync.HashBlockRequestMessage
import io.openfuture.chain.network.message.sync.HashBlockResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class HashBlockRequestHandler(
    private val blockService: BlockService
) : SimpleChannelInboundHandler<HashBlockRequestMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HashBlockRequestMessage) {
        val lastBlock = blockService.getLast()
        ctx.writeAndFlush(HashBlockResponseMessage(lastBlock.hash, msg.synchronizationSessionId))
    }

}
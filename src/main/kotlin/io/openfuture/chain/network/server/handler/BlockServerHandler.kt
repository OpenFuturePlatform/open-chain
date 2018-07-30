package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.network.domain.NetworkBlockRequest
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class BlockServerHandler(
    private val blockService: CommonBlockService
) : ServerHandler<NetworkBlockRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkBlockRequest) {
        val blocks = blockService.getBlocksAfterCurrentHash(message.hash)

        blocks?.forEach {
            when (it) {
                is MainBlock -> ctx.writeAndFlush(NetworkMainBlock(it))

                is GenesisBlock -> ctx.writeAndFlush(NetworkGenesisBlock(it))
            }
        }
    }

}

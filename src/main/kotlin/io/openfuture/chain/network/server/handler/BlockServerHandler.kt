package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.NetworkBlockRequest
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class BlockServerHandler(
    private val blockService: BlockService
) : BaseHandler<NetworkBlockRequest>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: NetworkBlockRequest) {
        val blocks = blockService.getBlocksAfterCurrentHash(message.hash)

        blocks?.forEach {
            when (it) {
                is MainBlock -> ctx.writeAndFlush(NetworkMainBlock(it))

                is GenesisBlock -> ctx.writeAndFlush(NetworkGenesisBlock(it))
            }
        }
    }

}

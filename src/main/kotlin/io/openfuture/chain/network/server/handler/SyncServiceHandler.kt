package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.network.domain.NetworkBlockRequest
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class SyncServerHandler(private val blockService: BlockService) : BaseHandler<NetworkBlockRequest>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: NetworkBlockRequest) {
        val blocks = blockService.getBlocksAfterCurrentHash(message.hash)

        blocks?.forEach {
            val block = NetworkBlock(it.height, it.previousHash, it.merkleHash, it.timestamp, it.typeId,
                it.hash, it.signature)
            ctx.writeAndFlush(block)
        }
    }

}
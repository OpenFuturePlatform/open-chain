package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*


@Component
@Scope("prototype")
class SyncClientHandler(
    private val blockService: BlockService
) : BaseHandler<NetworkBlock>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: NetworkBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        if (message.typeId == BlockType.MAIN.id) {
            val block = MainBlock(message.height, message.previousHash,
                message.merkleHash, message.timestamp, message.hash, message.signature, Collections.emptyList())
            blockService.save(block)
        } else {
            val block = GenesisBlock(message.height, message.previousHash, message.merkleHash,
                message.timestamp, message.hash, message.signature, Collections.emptyList(), 1, Collections.emptySet())
            blockService.save(block)
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }


    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.fireChannelInactive()
    }

}
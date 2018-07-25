package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.network.base.handler.CommonHandler
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GenesisBlockClientHandler(
    private val blockService: BlockService
) : CommonHandler<NetworkGenesisBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkGenesisBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        val delegates = message.activeDelegates.map { Delegate.of(it) }.toMutableSet()

        val block = GenesisBlock(message.height, message.previousHash, message.blockTimestamp, message.epochIndex,
            delegates).apply { signature = message.signature }

        blockService.save(block)
    }

}


package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.service.CommonBlockService
import io.openfuture.chain.service.GenesisBlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class GenesisBlockClientHandler(
    private val blockService: CommonBlockService,
    private val genesisBlockService: GenesisBlockService
) : ClientHandler<NetworkGenesisBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkGenesisBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        genesisBlockService.add(message)
    }

}


package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope("prototype")
class MainBlockClientHandler(
    private val blockService: BlockService
) : ClientHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, dto: NetworkMainBlock) {
        if (blockService.isExists(dto.hash)) {
            return
        }

        blockService.add(dto)
    }

}


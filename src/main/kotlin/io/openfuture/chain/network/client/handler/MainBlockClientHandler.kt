package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope("prototype")
class MainBlockClientHandler(
    private val blockService: CommonBlockService,
    private val mainBlockService: MainBlockService
) : ClientHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, dto: NetworkMainBlock) {
        if (blockService.isExists(dto.hash)) {
            return
        }

        mainBlockService.add(dto)
    }

}


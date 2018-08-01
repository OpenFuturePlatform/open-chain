package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.network.domain.NetworkBlockApproval
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class BlockApprovalMessageHandler(
    val pendingBlockHandler: PendingBlockHandler
) : ClientHandler<NetworkBlockApproval>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkBlockApproval) {
        pendingBlockHandler.handleApproveMessage(message)
    }

}
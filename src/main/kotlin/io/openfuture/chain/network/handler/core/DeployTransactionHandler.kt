package io.openfuture.chain.network.handler.core

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.transaction.DefaultSmartContractTransactionService
import io.openfuture.chain.network.message.core.DeployTransactionMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class DeployTransactionHandler(
    private val transactionService: DefaultSmartContractTransactionService
) : SimpleChannelInboundHandler<DeployTransactionMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DeployTransactionMessage) {
        transactionService.add(msg)
    }

}
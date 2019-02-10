package io.openfuture.chain.network.handler.core

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class DelegateTransactionHandler(
    private val delegateTransactionService: DelegateTransactionService
) : SimpleChannelInboundHandler<DelegateTransactionMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DelegateTransactionMessage) {
        delegateTransactionService.add(UnconfirmedDelegateTransaction.of(msg))
    }

}
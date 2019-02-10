package io.openfuture.chain.network.handler.core

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class TransferTransactionHandler(
    private val transferTransactionService: TransferTransactionService
) : SimpleChannelInboundHandler<TransferTransactionMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TransferTransactionMessage) {
        transferTransactionService.add(UnconfirmedTransferTransaction.of(msg))
    }

}
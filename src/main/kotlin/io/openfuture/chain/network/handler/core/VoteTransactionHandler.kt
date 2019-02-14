package io.openfuture.chain.network.handler.core

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class VoteTransactionHandler(
    private val transactionManager: TransactionManager
) : SimpleChannelInboundHandler<VoteTransactionMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: VoteTransactionMessage) {
        transactionManager.add(UnconfirmedVoteTransaction.of(msg))
    }

}
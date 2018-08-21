package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import org.springframework.stereotype.Service

@Service
class DefaultCoreMessageService(
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService
) : CoreMessageService {

    @BlockchainSynchronized
    override fun onTransferTransaction(ctx: ChannelHandlerContext, message: TransferTransactionMessage) {
        transferTransactionService.add(message)
    }

    @BlockchainSynchronized
    override fun onDelegateTransaction(ctx: ChannelHandlerContext, message: DelegateTransactionMessage) {
        delegateTransactionService.add(message)
    }

    @BlockchainSynchronized
    override fun onVoteTransaction(ctx: ChannelHandlerContext, message: VoteTransactionMessage) {
        voteTransactionService.add(message)
    }

}

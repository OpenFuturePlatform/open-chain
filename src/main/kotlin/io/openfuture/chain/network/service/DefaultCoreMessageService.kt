package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.NetworkAddressMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class DefaultCoreMessageService(
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateService: DelegateService
) : CoreMessageService {

    override fun onTransferTransaction(ctx: ChannelHandlerContext, tx: TransferTransactionMessage) {
        transferTransactionService.add(tx)
    }

    override fun onDelegateTransaction(ctx: ChannelHandlerContext, tx: DelegateTransactionMessage) {
        delegateTransactionService.add(tx)
    }

    override fun onVoteTransaction(ctx: ChannelHandlerContext, tx: VoteTransactionMessage) {
        voteTransactionService.add(tx)
    }

    override fun onFindDelegates(ctx: ChannelHandlerContext, delegateRequestMessage: DelegateRequestMessage) {
        val delegates = delegateService.getAll(PageRequest())
        val delegateNodeAddresses = delegates.stream().map { NetworkAddressMessage(it.host, it.port) }.collect(Collectors.toList())

        ctx.writeAndFlush(DelegateResponseMessage(delegateNodeAddresses, delegateRequestMessage.synchronizationSessionId))
    }

}

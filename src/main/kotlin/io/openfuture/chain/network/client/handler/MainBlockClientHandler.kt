package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class MainBlockClientHandler(
    private val pendingBlockHandler: PendingBlockHandler
) : ClientHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, networkBlock: NetworkMainBlock) {
        val block = MainBlock(
            networkBlock.height,
            networkBlock.previousHash,
            networkBlock.timestamp!!,
            networkBlock.reward,
            networkBlock.publicKey,
            networkBlock.merkleHash,
            toTransactions(
                networkBlock.transferTransactions,
                networkBlock.voteTransactions,
                networkBlock.delegateTransactions)
        )
        pendingBlockHandler.addBlock(block)
    }

    private fun toTransactions(
        transferTransactions: MutableList<TransferTransactionDto>,
        voteTransactions: MutableList<VoteTransactionDto>,
        delegateTransactions: MutableList<DelegateTransactionDto>
    ): MutableSet<Transaction> {
        return mutableSetOf(
            *transferTransactions.map { it.toEntity() }.toTypedArray(),
            *voteTransactions.map { it.toEntity() }.toTypedArray(),
            *delegateTransactions.map { it.toEntity() }.toTypedArray()
        )
    }

}


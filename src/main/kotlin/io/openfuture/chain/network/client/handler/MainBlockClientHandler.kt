package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.consensus.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.RewardTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope("prototype")
class MainBlockClientHandler(
    private val pendingBlockHandler: PendingBlockHandler
) : ClientHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, networkBlock: NetworkMainBlock) {
        val block = MainBlock(
            networkBlock.height,
            networkBlock.previousHash,
            networkBlock.timestamp!!,
            networkBlock.publicKey,
            toTransactions(
                networkBlock.transferTransactions,
                networkBlock.voteTransactions,
                networkBlock.delegateTransactions,
                networkBlock.rewardTransactions),
            networkBlock.merkleHash
        )
        pendingBlockHandler.addBlock(block)
    }

    private fun toTransactions(
            transferTransactions: MutableList<TransferTransactionDto>,
            voteTransactions: MutableList<VoteTransactionDto>,
            delegateTransactions: MutableList<DelegateTransactionDto>,
            rewardTransactions: MutableList<RewardTransactionDto>): MutableSet<Transaction> {
        val transferTransactionEntities = transferTransactions.map { it.toEntity() }.toSet()
        val voteTransactionEntities = voteTransactions.map { it.toEntity() }.toSet()
        val delegateTransactionEntities = delegateTransactions.map { it.toEntity() }.toSet()
        val rewardTransactionEntities = rewardTransactions.map { it.toEntity() }.toSet()

        val transactions = HashSet<Transaction>()
        transactions.addAll(transferTransactionEntities)
        transactions.addAll(voteTransactionEntities)
        transactions.addAll(delegateTransactionEntities)
        transactions.addAll(rewardTransactionEntities)
        return transactions
    }

}


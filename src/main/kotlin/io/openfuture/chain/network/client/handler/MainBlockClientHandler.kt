package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*


@Component
@Scope("prototype")
class MainBlockClientHandler(
    private val blockService: BlockService,
    private val transferTransactionService: BaseTransactionService<TransferTransaction>,
    private val voteTransactionService: BaseTransactionService<VoteTransaction>
) : ClientHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkMainBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        val block = MainBlock(message.height, message.previousHash,
            message.merkleHash, message.blockTimestamp, Collections.emptyList()).apply { signature = message.signature }

        val savedBlock = blockService.save(block)

        val transferTransactions = message.transferTransactions
            .filter { transferTransactionService.isExists(it.hash) }
            .map { TransferTransaction.of(it).apply { this.block = savedBlock } }
        transferTransactionService.save(transferTransactions)

        val voteTransactions = message.voteTransactions
            .filter { voteTransactionService.isExists(it.hash) }
            .map { VoteTransaction.of(it).apply { this.block = savedBlock } }
        voteTransactionService.save(voteTransactions)
    }

}


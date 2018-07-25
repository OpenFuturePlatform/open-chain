package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.base.handler.CommonHandler
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope("prototype")
class MainBlockClientHandler(
    private val blockService: BlockService
) : CommonHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, message: NetworkMainBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        val transferTransactions = message.transferTransactions.map { TransferTransaction.of(it) }
        val voteTransactions = message.voteTransactions.map { VoteTransaction.of(it) }
        val transactions = listOf(transferTransactions, voteTransactions).flatten().toMutableList()

        val block = MainBlock(message.height, message.previousHash,
            message.merkleHash, message.blockTimestamp, transactions).apply { signature = message.signature }

        blockService.save(block)
    }

}


package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.TransferTransactionService
import io.openfuture.chain.service.VoteTransactionService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*


@Component
@Scope("prototype")
class MainBlockClientHandler(
    private val blockService: BlockService,
    private val transferTransactionService: TransferTransactionService,
    private val voteTransactionService: VoteTransactionService
    ) : ClientHandler<NetworkMainBlock>() {

    override fun channelRead0(ctx: ChannelHandlerContext, dto: NetworkMainBlock) {
        if (blockService.isExists(dto.hash)) {
            return
        }

        val entity = MainBlock(dto.height, dto.previousHash,
            dto.merkleHash, dto.blockTimestamp, Collections.emptyList()).apply { signature = dto.signature }

        val savedBlock = blockService.save(entity)

        dto.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
        dto.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }

    }

}


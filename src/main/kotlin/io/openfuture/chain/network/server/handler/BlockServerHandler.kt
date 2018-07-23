package io.openfuture.chain.network.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.network.domain.NetworkBlockRequest
import io.openfuture.chain.network.domain.NetworkTransaction
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class BlockServerHandler(
    private val blockService: BlockService,
    private val transferTransactionService: BaseTransactionService<TransferTransaction>
) : BaseHandler<NetworkBlockRequest>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: NetworkBlockRequest) {
        val blocks = blockService.getBlocksAfterCurrentHash(message.hash)

        blocks?.forEach {
            var networkTransactions = listOf<NetworkTransaction>()
            if(it.typeId == BlockType.MAIN.id) {
                val transactions = transferTransactionService.getByBlock(it as MainBlock)
                networkTransactions = transactions.map {
                    NetworkTransaction(it.timestamp, it.amount, it.fee, it.recipientAddress,
                        it.senderKey, it.senderAddress, it.senderSignature!!, it.hash)
                }
            }
            val block = NetworkBlock(it.height, it.previousHash, it.merkleHash, it.timestamp, it.typeId,
                it.hash, it.signature!!, listOf<NetworkTransaction>() as MutableList<NetworkTransaction>)
            ctx.writeAndFlush(block)
        }
    }

}
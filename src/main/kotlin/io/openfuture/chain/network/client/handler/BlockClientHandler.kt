package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*


@Component
@Scope("prototype")
class BlockClientHandler(
    private val blockService: BlockService,
    private val transactionService: BaseTransactionService<TransferTransaction>
) : BaseHandler<NetworkBlock>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: NetworkBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        if (message.typeId == BlockType.MAIN.id) {
            val block = MainBlock(message.height, message.previousHash,
                message.merkleHash, message.timestamp, Collections.emptyList()).apply { signature = message.signature }

            checkHash(message.hash, block.hash)
            val savedBlock = blockService.save(block)

            message.transactions.forEach {
                val transaction = TransferTransaction(it.timestamp, it.amount, it.fee, it.recipientAddress, it.senderKey,
                    it.senderAddress, null, it.senderSignature, savedBlock)

                checkHash(message.hash, transaction.hash)

                transactionService.save(transaction)
            }
        } else {
            val block = GenesisBlock(message.height, message.previousHash, message.timestamp, 1, Collections.emptySet())
                .apply { signature = message.signature }

            checkHash(message.hash, block.hash)

            blockService.save(block)
        }
    }

    private fun checkHash(hash: String, calculatedHash: String) {
//        if(hash != calculatedHash) {
//            throw LogicException("Block synchronization error, incorrect hash")
//        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.fireChannelInactive()
    }

}
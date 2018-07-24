package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.service.BlockService
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


@Component
@Scope("prototype")
class BlockClientHandler(
    private val blockService: BlockService
) : BaseHandler<NetworkBlock>() {

    override fun packetReceived(ctx: ChannelHandlerContext, message: NetworkBlock) {
        if (blockService.isExists(message.hash)) {
            return
        }

        when (message) {
            is NetworkMainBlock -> {
                val transferTransactions = message.transferTransactions.map {  TransferTransaction(it.timestamp, it.amount,
                    it.fee, it.recipientAddress, it.senderKey, it.senderAddress, null, it.senderSignature) }.toMutableList()
                val voteTransactions = message.voteTransactions.map {  VoteTransaction(it.timestamp, it.amount, it.fee, it.recipientAddress,
                    it.senderKey, it.senderAddress, it.voteTypeId, it.delegateHost, it.delegatePort, null, it.senderSignature) }.toMutableList()
                val transactions = listOf(transferTransactions, voteTransactions).flatten().toMutableList()
                val block = MainBlock(message.height, message.previousHash,
                    message.merkleHash, message.timestamp, transactions).apply { signature = message.signature }

                blockService.save(block)
            }

            is NetworkGenesisBlock -> {
                val delegates = message.activeDelegates.map { Delegate(it.host!!, it.port!!, it.rating!!) }.toMutableSet()

                val block = GenesisBlock(message.height, message.previousHash, message.timestamp, message.epochIndex,
                    delegates).apply { signature = message.signature }

                blockService.save(block)
            }

        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.fireChannelInactive()
    }

}


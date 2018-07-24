package io.openfuture.chain.network.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.network.base.BaseHandler
import io.openfuture.chain.network.domain.*
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
                val transactions = message.transactions.map { getTransaction(it) }.toMutableList()

                val block = MainBlock(message.height, message.previousHash,
                    message.merkleHash, message.timestamp, transactions).apply { signature = message.signature }

                blockService.save(block)
            }

            is NetworkGenesisBlock -> {
                val delegates = message.activeDelegates.map { Delegate(it.host, it.port, it.rating) }.toMutableSet()

                val block = GenesisBlock(message.height, message.previousHash, message.timestamp, message.epochIndex,
                    delegates).apply { signature = message.signature }

                blockService.save(block)
            }

        }
    }

    private fun getTransaction(tr: NetworkTransaction): BaseTransaction {
        return when (tr) {
            is NetworkTransferTransaction -> TransferTransaction(tr.timestamp, tr.amount,
                tr.fee, tr.recipientAddress, tr.senderKey, tr.senderAddress, null, tr.senderSignature)

            is NetworkVoteTransaction -> VoteTransaction(tr.timestamp, tr.amount, tr.fee, tr.recipientAddress,
                tr.senderKey, tr.senderAddress, tr.voteTypeId, tr.delegateHost, tr.delegatePort)
                .apply { senderSignature = tr.senderSignature }

            else -> throw LogicException("Incorrect transaction type")
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.fireChannelInactive()
    }

}


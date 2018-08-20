package io.openfuture.chain.network.sync.impl

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.NetworkAddressMessage
import io.openfuture.chain.network.sync.SyncBlockRequestHandler
import org.springframework.stereotype.Component

@Component
class DefaultSyncBlockRequestHandler(
    private val blockService: BlockService,
    private val genesisBlockService: GenesisBlockService
): SyncBlockRequestHandler {

    override fun onDelegateRequestMessage(ctx: ChannelHandlerContext, message: DelegateRequestMessage) {
        val addresses = genesisBlockService.getLast().payload.activeDelegates
            .map { NetworkAddressMessage(it.host, it.port)  }
        send(ctx, DelegateResponseMessage(addresses, message.synchronizationSessionId))
    }

    override fun onLastHashRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage) {
        val lastBlock = blockService.getLast()
        send(ctx, HashBlockResponseMessage(lastBlock.hash, message.synchronizationSessionId))
    }

    override fun onSyncBlocRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage) {
        blockService.getAfterCurrentHash(message.hash)
            .map { it.toMessage() }
            .forEach { msg -> send(ctx, msg) }
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)

}
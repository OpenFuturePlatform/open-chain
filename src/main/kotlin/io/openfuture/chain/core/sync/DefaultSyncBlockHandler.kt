package io.openfuture.chain.core.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
class DefaultSyncBlockHandler(
    @Lazy private val blockService: BlockService,
    private val lock: ReentrantReadWriteLock,
    @Lazy private val mainBlockService: MainBlockService,
    @Lazy private val networkApiService: NetworkApiService,
    @Lazy private val genesisBlockService: GenesisBlockService
) : SyncBlockHandler {

    private var blockHash: String? = null


    override fun blockHashRequest(ctx: ChannelHandlerContext, message: HashBlockRequestMessage) {
        if (!blockService.isExists(message.hash)) {
            send(ctx, HashBlockResponseMessage(message.hash))
            return
        }

        val lastBlock = blockService.getLast()
        send(ctx, HashBlockResponseMessage(lastBlock.hash))
    }

    override fun blockHashResponse(ctx: ChannelHandlerContext, message: HashBlockResponseMessage) {
        val lastHash = blockService.getLast().hash

        if (message.hash != lastHash) {
            blockHash = message.hash

            send(ctx, SyncBlockRequestMessage(lastHash))
        } else {
            lock.writeLock().unlock()
        }
    }

    override fun getBlocks(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage) {
        blockService.getAfterCurrentHash(message.hash)
            .map { it.toMessage() }
            .forEach { block -> send(ctx, block) }
    }

    override fun saveBlocks(block: MainBlockMessage) {
        mainBlockService.synchronize(block)

        unlockIfAllBlocksSynchronized(block)
    }


    override fun saveBlocks(block: GenesisBlockMessage) {
        genesisBlockService.add(block)

        unlockIfAllBlocksSynchronized(block)
    }

    override fun sync() {
        try {
            lock.writeLock().lock()
            networkApiService.send(HashBlockRequestMessage(blockService.getLast().hash))
        } catch (e: Exception) {
            lock.writeLock().unlock()
            sync()
        }
    }

    private fun unlockIfAllBlocksSynchronized(block: BlockMessage) {
        if (block.hash == blockHash) {
            lock.writeLock().unlock()
        }
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)


}

package io.openfuture.chain.core.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
class DefaultSyncBlockHandler(
    private val blockService: BlockService,
    private val lock: ReentrantReadWriteLock,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService
) : SyncBlockHandler {

    private var blockHash: String? = null


    override fun blockHashRequest(ctx: ChannelHandlerContext, message: HashBlockRequestMessage) {
        val lastHash = blockService.getLastHashAfterCurrent(message.hash)

        send(ctx, HashBlockResponseMessage(lastHash))
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
        val blocks = blockService.getAfterCurrentHash(message.hash)

        blocks.forEach { block -> send(ctx, block) }
    }

    override fun saveBlocks(block: MainBlockMessage) {
        mainBlockService.synchronize(block)

        unlockIfAllBlockSynchronized(block)
    }


    override fun saveBlocks(block: GenesisBlockMessage) {
        genesisBlockService.add(block)

        unlockIfAllBlockSynchronized(block)
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

    private fun unlockIfAllBlockSynchronized(block: BlockMessage) {
        if (block.hash == blockHash) {
            lock.writeLock().unlock()
        }
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)


}

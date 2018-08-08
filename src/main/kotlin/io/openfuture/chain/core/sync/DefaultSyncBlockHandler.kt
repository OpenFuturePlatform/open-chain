package io.openfuture.chain.core.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
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
        val lastHash = blockService.getLast().hash

        ctx.channel().writeAndFlush(HashBlockResponseMessage(lastHash))
    }

    override fun blockHashResponse(ctx: ChannelHandlerContext, message: HashBlockResponseMessage) {
        val lastHash = blockService.getLast().hash
        if (lastHash != message.hash) {
            blockHash = message.hash
            ctx.channel().writeAndFlush(SyncBlockRequestMessage(lastHash))
        }
    }

    override fun getBlocks(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage) {
        val blocks = blockService.getLast()

        ctx.channel().writeAndFlush(blocks)
    }

    override fun saveBlocks(block: MainBlockMessage) {
        mainBlockService.synchronize(block)
        if (block.hash == blockHash) {
            lock.writeLock().unlock()
        }
    }

    override fun saveBlocks(block: GenesisBlockMessage) {
        genesisBlockService.synchronize(block)
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

}

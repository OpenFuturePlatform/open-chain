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
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PostConstruct

@Service
class DefaultSyncBlockHandler(
    @Lazy private val blockService: BlockService,
//    private val lock: ReentrantReadWriteLock,
    @Lazy private val mainBlockService: MainBlockService,
    @Lazy private val networkApiService: NetworkApiService,
    @Lazy private val genesisBlockService: GenesisBlockService
) : SyncBlockHandler {


    private var syncStatus: AtomicBoolean = AtomicBoolean(false)
    private lateinit var lastHash: String


    @PostConstruct
    fun initSyncStatus() {
        lock()
        lastHash = blockService.getLast().hash
    }

    @Synchronized
    override fun isSynchronize(): Boolean {
        return syncStatus.get()
    }

    override fun handleHashBlockRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage) {
        val lastBlock = blockService.getLast()
        send(ctx, HashBlockResponseMessage(lastBlock.hash))
    }

    @Synchronized
    override fun handleHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage) {
        if (blockService.isExists(message.hash)) {
            unlock(message.hash)
            return
        }
        send(ctx, SyncBlockRequestMessage(lastHash))
    }

    @Synchronized
    override fun handleSyncBlocKRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage) {
        blockService.getAfterCurrentHash(message.hash)
            .map { it.toMessage() }
            .forEach { block -> send(ctx, block) }
    }

    @Synchronized
    override fun handleMainBlockMessage(block: MainBlockMessage) {
        mainBlockService.synchronize(block)

        if (blockService.getLast().hash == block.hash) {
            unlock(block.hash)
        }

    }


    @Synchronized
    override fun handleGenesisBlockMessage(block: GenesisBlockMessage) {
        genesisBlockService.add(block)

        if (blockService.getLast().hash == block.hash) {
            unlock(block.hash)
        }
    }

    override fun synchronize() {
        networkApiService.send(HashBlockRequestMessage(lastHash))
    }

    private fun lock() {
        syncStatus.set(false)
    }

    private fun unlock(hash: String) {
        lastHash = hash
        syncStatus.set(true)
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)


}

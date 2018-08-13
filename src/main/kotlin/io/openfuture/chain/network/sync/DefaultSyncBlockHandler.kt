package io.openfuture.chain.network.sync

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.handler.BaseConnectionHandler
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SynchronizationStatus.*
import org.apache.commons.lang3.tuple.MutablePair
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Service
class DefaultSyncBlockHandler(
    private val blockService: BlockService,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService
) : SyncBlockHandler {

    @Volatile
    private var syncStatus: SynchronizationStatus = NOT_SYNCHRONIZED

    @Volatile
    private lateinit var expectedHashAndResponseTime: MutablePair<String, Long>

    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    override fun getSyncStatus(): SynchronizationStatus {
        lock.readLock().lock()
        try {
            return syncStatus
        } finally {
            lock.readLock().unlock()
        }
    }

    override fun getLastResponseTime(): Long = expectedHashAndResponseTime.right

    @Synchronized
    override fun onHashBlockRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage) {
        val lastBlock = blockService.getLast()
        send(ctx, HashBlockResponseMessage(lastBlock.hash))
    }

    @Synchronized
    override fun onSyncBlocRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage) {
        blockService.getAfterCurrentHash(message.hash)
            .map { it.toMessage() }
            .forEach { msg -> send(ctx, msg) }
    }

    @Synchronized
    override fun onHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage) {
        val currentLastHash = blockService.getLast().hash
        if (currentLastHash == message.hash) {
            unlock()
            return
        } else {
            expectedHashAndResponseTime.left = message.hash
            expectedHashAndResponseTime.right = System.currentTimeMillis()
        }
        send(ctx, SyncBlockRequestMessage(currentLastHash))
    }

    @Synchronized
    override fun onMainBlockMessage(block: MainBlockMessage) {
        mainBlockService.synchronize(block)
        unlockIfLastBLock(block)
    }

    @Synchronized
    override fun onGenesisBlockMessage(block: GenesisBlockMessage) {
        genesisBlockService.synchronize(block)
        unlockIfLastBLock(block)
    }

    @Synchronized
    override fun synchronize() {
        try {
            processing()
            networkApiService.send(HashBlockRequestMessage(blockService.getLast().hash))
        } catch (e: Exception) {
            synchronize()
            log.error(e.message)
        }
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)

    private fun unlockIfLastBLock(block: BlockMessage) {
        if (expectedHashAndResponseTime.left == block.hash) {
            unlock()
        } else {
            expectedHashAndResponseTime.right = System.currentTimeMillis()
        }
    }

    private fun processing() {
        changeSynchronizationStatus(PROCESSING)
    }

    private fun unlock() {
        changeSynchronizationStatus(SYNCHRONIZED)
    }

    private fun changeSynchronizationStatus(status: SynchronizationStatus) {
        lock.writeLock().lock()
        try {
            syncStatus = status
        } finally {
            lock.writeLock().unlock()
        }
    }

}

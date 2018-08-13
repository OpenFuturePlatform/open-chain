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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.annotation.PostConstruct

@Service
class DefaultSyncBlockHandler(
    private val blockService: BlockService,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService
) : SyncBlockHandler {

    @Volatile
    private lateinit var syncStatus: SynchronizationStatus

    @Volatile
    private lateinit var expectedHash: String

    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    @PostConstruct
    fun initSyncStatus() {
        lock(blockService.getLast().hash)
    }

    override fun getSyncStatus(): SynchronizationStatus {
        lock.readLock().lock()
        try {
            return syncStatus
        } finally {
            lock.readLock().unlock()
        }
    }

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
        if (expectedHash == message.hash) {
            unlock(message.hash)
            return
        } else {
            expectedHash = message.hash
        }
        send(ctx, SyncBlockRequestMessage(blockService.getLast().hash))
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
            val hash = blockService.getLast().hash
            processing(hash)
            networkApiService.send(HashBlockRequestMessage(hash))
        } catch (e: Exception) {
            synchronize()
            log.error(e.message)
        }
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)

    private fun unlockIfLastBLock(block: BlockMessage) {
        if (expectedHash == block.hash) {
            unlock(block.hash)
        }
    }

    private fun lock(hash: String) {
        changeSynchronizationStatus(hash, NOT_SYNCHRONIZED)
    }

    private fun processing(hash: String) {
        changeSynchronizationStatus(hash, PROCESSING)
    }

    private fun unlock(hash: String) {
        changeSynchronizationStatus(hash, SYNCHRONIZED)
    }

    private fun changeSynchronizationStatus(hash: String, status: SynchronizationStatus) {
        lock.writeLock().lock()
        try {
            expectedHash = hash
            syncStatus = status
        } finally {
            lock.writeLock().unlock()
        }
    }

}

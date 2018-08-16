package io.openfuture.chain.network.sync.impl

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.handler.BaseConnectionHandler
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SyncBlockHandler
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.PROCESSING
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.SYNCHRONIZED
import org.apache.commons.lang3.tuple.MutablePair
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultSyncBlockHandler(
    private val blockService: BlockService,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val syncManager: SyncManager
) : SyncBlockHandler {

    @Volatile
    private var expectedHashAndResponseTime: MutablePair<String, Long> = MutablePair()

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    @Synchronized
    override fun getLastResponseTime(): Long = expectedHashAndResponseTime.right

    @Synchronized
    override fun onDelegateResponseMessage(ctx: ChannelHandlerContext, message: DelegateResponseMessage) {
        val lastBlockHash = blockService.getLast().hash
        val randomAddress = message.addresses.shuffled().first()

        networkApiService.sendToAddress(HashBlockRequestMessage(lastBlockHash), randomAddress)
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
        val currentLastHash = blockService.getLast().hash
        if (currentLastHash == message.hash) {
            unlock()
            return
        } else {
            expectedHashAndResponseTime.left = message.hash
        }
        send(ctx, SyncBlockRequestMessage(currentLastHash))
    }

    @Synchronized
    override fun onMainBlockMessage(block: MainBlockMessage) {
        mainBlockService.add(block)
        unlockIfLastBLock(block)
    }

    @Synchronized
    override fun onGenesisBlockMessage(block: GenesisBlockMessage) {
        genesisBlockService.add(block)
        unlockIfLastBLock(block)
    }

    @Synchronized
    override fun synchronize() {
        try {
            processing()
            networkApiService.sendToRootNode(DelegateRequestMessage())
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
        syncManager.setSyncStatus(PROCESSING)
        expectedHashAndResponseTime.right = System.currentTimeMillis()
    }

    private fun unlock() {
        syncManager.setSyncStatus(SYNCHRONIZED)
    }


}

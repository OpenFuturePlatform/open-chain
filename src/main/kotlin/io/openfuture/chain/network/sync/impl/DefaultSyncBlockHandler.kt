package io.openfuture.chain.network.sync.impl

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.handler.BaseConnectionHandler
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.NetworkAddressMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SyncBlockHandler
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.PROCESSING
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.SYNCHRONIZED
import org.apache.commons.lang3.RandomStringUtils.random
import org.apache.commons.lang3.tuple.MutablePair
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.stream.Collectors


@Service
class DefaultSyncBlockHandler(
    private val blockService: BlockService,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val syncManager: SyncManager
) : SyncBlockHandler {

    @Volatile
    private lateinit var synchronizationSessionId : String

    @Volatile
    private var expectedHashAndResponseTime : MutablePair<String, Long> = MutablePair()

    @Volatile
    private lateinit var delegates: ConcurrentHashMap<NetworkAddressMessage, String>

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    @Synchronized
    override fun getLastResponseTime(): Long = expectedHashAndResponseTime.right

    @Synchronized
    override fun onDelegateResponseMessage(ctx: ChannelHandlerContext, message: DelegateResponseMessage) {
        if(message.synchronizationSessionId != synchronizationSessionId) {
            return
        }

        val lastBlockHash = blockService.getLast().hash

        message.addresses.forEach { networkApiService.sendToAddress(HashBlockRequestMessage(lastBlockHash, synchronizationSessionId), it) }
    }

    @Synchronized
    override fun onHashBlockRequestMessage(ctx: ChannelHandlerContext, message: HashBlockRequestMessage) {
        val lastBlock = blockService.getLast()
        send(ctx, HashBlockResponseMessage(lastBlock.hash, message.synchronizationSessionId))
    }

    @Synchronized
    override fun onSyncBlocRequestMessage(ctx: ChannelHandlerContext, message: SyncBlockRequestMessage) {
        blockService.getAfterCurrentHash(message.hash)
            .map { it.toMessage() }
            .forEach { msg -> send(ctx, msg) }
    }

    @Synchronized
    override fun onHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage) {
        if(message.synchronizationSessionId != synchronizationSessionId) {
            return
        }

        val currentDelegate = getCurrentDelegate(ctx)
        currentDelegate.setValue(message.hash)

        val correctLastHash = getCorrectLastHash()

        if (correctLastHash.isPresent) {
            val currentLastHash = blockService.getLast().hash
            if (currentLastHash == message.hash) {
                unlock()
                return
            } else {
                expectedHashAndResponseTime.left = message.hash
            }

            val delegateAddress = delegates.entries.first { it.value == correctLastHash.get().key }

            networkApiService.sendToAddress(SyncBlockRequestMessage(blockService.getLast().hash), delegateAddress.key)
        }
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
            networkApiService.sendToRootNode(DelegateRequestMessage(synchronizationSessionId))
        } catch (e: Exception) {
            synchronize()
            log.error(e.message)
        }
    }

    private fun send(ctx: ChannelHandlerContext, message: BaseMessage) = ctx.channel().writeAndFlush(message)

    private fun getCorrectLastHash(): Optional<MutableMap.MutableEntry<String, Long>> {
        val uniqueLastHashes = delegates.values.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))

        return uniqueLastHashes.entries.stream().filter { it.value > delegates.size * 2 / 3 }.findFirst()
    }

    private fun getCurrentDelegate(ctx: ChannelHandlerContext): MutableMap.MutableEntry<NetworkAddressMessage, String> {
        val host = (ctx.channel().remoteAddress() as InetSocketAddress).address.hostAddress
        val port = (ctx.channel().remoteAddress() as InetSocketAddress).port
        return delegates.entries.first { it.key.host == host && it.key.port == port }
    }

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
        delegates.clear()
        synchronizationSessionId = random(10)
    }

    private fun unlock() {
        syncManager.setSyncStatus(SYNCHRONIZED)
    }


}

package io.openfuture.chain.network.sync.impl

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.handler.BaseConnectionHandler
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.NetworkAddressMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.network.sync.SyncBlockResponseHandler
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.PROCESSING
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.SYNCHRONIZED
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap


@Service
class DefaultSyncBlockResponseHandler(
    private val blockService: BlockService,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val syncManager: SyncManager
) : SyncBlockResponseHandler {

    @Volatile
    private var synchronizationSessionId : String = System.currentTimeMillis().toString()

    @Volatile
    private var activeDelegateAddresses: MutableList<NetworkAddressMessage> = mutableListOf()

    @Volatile
    private var activeDelegatesLastHash: ConcurrentHashMap<String, MutableList<NetworkAddressMessage>> = ConcurrentHashMap()

    @Volatile
    private var expectedHash : String = EMPTY

    @Volatile
    private var lastResponseTime : Long = System.currentTimeMillis()


    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }

    @Synchronized
    override fun getLastResponseTime(): Long = lastResponseTime

    @Synchronized
    override fun onDelegateResponseMessage(message: DelegateResponseMessage) {
        if (message.synchronizationSessionId != synchronizationSessionId || !activeDelegateAddresses.isEmpty()) {
            return
        }

        activeDelegateAddresses.addAll(message.addresses)
        activeDelegateAddresses.forEach { networkApiService.sendToAddress(HashBlockRequestMessage(synchronizationSessionId), it) }
    }

    @Synchronized
    override fun onHashResponseMessage(ctx: ChannelHandlerContext, message: HashBlockResponseMessage) {
        if (message.synchronizationSessionId != synchronizationSessionId) {
            return
        }

        val delegateAddresses = activeDelegatesLastHash[message.hash]
        val address = getNetworkAddress(ctx)
        if (null != delegateAddresses && !delegateAddresses.contains(address)) {
            delegateAddresses.add(getNetworkAddress(ctx))
            if (delegateAddresses.size > (activeDelegateAddresses.size - 1) / 3 * 2) {
                val currentLastHash = blockService.getLast().hash
                if (currentLastHash == message.hash) {
                    unlock()
                    return
                } else {
                    expectedHash = message.hash
                    networkApiService.sendToAddress(SyncBlockRequestMessage(blockService.getLast().hash), address)
                }
            }
        } else {
            activeDelegatesLastHash[message.hash] = mutableListOf(address)
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

    private fun unlockIfLastBLock(block: BlockMessage) {
        if (expectedHash == block.hash) {
            unlock()
        } else {
            lastResponseTime = System.currentTimeMillis()
        }
    }

    private fun getNetworkAddress(ctx: ChannelHandlerContext): NetworkAddressMessage {
        val remoteAddress = ctx.channel().remoteAddress() as InetSocketAddress
        return NetworkAddressMessage(remoteAddress.address.hostAddress, remoteAddress.port)
    }

    private fun processing() {
        reset()
        syncManager.setSyncStatus(PROCESSING)
    }

    private fun reset() {
        activeDelegateAddresses.clear()
        activeDelegatesLastHash.clear()
        expectedHash = EMPTY
        lastResponseTime = System.currentTimeMillis()
        synchronizationSessionId = System.currentTimeMillis().toString()
    }

    private fun unlock() {
        syncManager.setSyncStatus(SYNCHRONIZED)
    }

}

package io.openfuture.chain.core.sync

import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.PROCESSING
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.SYNCHRONIZED
import io.openfuture.chain.network.component.NodeClock
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.core.BlockMessage
import io.openfuture.chain.network.message.sync.*
import io.openfuture.chain.network.service.NetworkApiService
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class SyncManager(
    private val blockService: BlockService,
    private val networkApiService: NetworkApiService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val syncStatus: SyncStatus,
    private val nodeClock: NodeClock
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncManager::class.java)
    }

    @Volatile
    private var sessionId: String = UUID.randomUUID().toString()

    @Volatile
    private var activeDelegateAddresses: MutableList<NodeInfo> = mutableListOf()

    @Volatile
    private var activeDelegatesLastHash: ConcurrentHashMap<String, MutableList<NodeInfo>> = ConcurrentHashMap()

    @Volatile
    private var expectedHash: String = EMPTY

    @Volatile
    private var lastResponseTime: Long = nodeClock.networkTime()

    @Volatile
    private var unlocked: Boolean = false


    @Synchronized
    fun getLastResponseTime(): Long = lastResponseTime

    @Synchronized
    fun onDelegateResponseMessage(message: DelegateResponseMessage) {
        if (message.synchronizationSessionId != sessionId || !activeDelegateAddresses.isEmpty()) {
            return
        }

        activeDelegateAddresses.addAll(message.nodesInfo)
        activeDelegateAddresses.forEach { networkApiService.sendToAddress(HashBlockRequestMessage(sessionId), it) }
    }

    @Synchronized
    fun onHashResponseMessage(message: HashBlockResponseMessage, nodeInfo: NodeInfo) {
        if (message.synchronizationSessionId != sessionId) {
            return
        }

        val delegateAddresses = activeDelegatesLastHash[message.hash]
        if (null != delegateAddresses && !delegateAddresses.contains(nodeInfo)) {
            delegateAddresses.add(nodeInfo)
            if (delegateAddresses.size > (activeDelegateAddresses.size - 1) / 3 * 1) {
                val currentLastHash = blockService.getLast().hash
                if (currentLastHash == message.hash) {
                    unlock()
                    return
                } else {
                    expectedHash = message.hash
                    networkApiService.sendToAddress(SyncBlockRequestMessage(currentLastHash), nodeInfo)
                }
            }
        } else {
            activeDelegatesLastHash[message.hash] = mutableListOf(nodeInfo)
        }
    }

    @Synchronized
    fun synchronize() {
        try {
            processing()
            networkApiService.sendRandom(DelegateRequestMessage(sessionId))
        } catch (e: Exception) {
            synchronize()
            log.warn(e.message)
        }
    }

    @Synchronized
    fun onMainBlockMessage(block: MainBlockMessage) {
        mainBlockService.add(block)
        unlockIfLastBLock(block)
    }

    @Synchronized
    fun onGenesisBlockMessage(block: GenesisBlockMessage) {
        genesisBlockService.add(block)
        unlockIfLastBLock(block)
    }

    private fun unlockIfLastBLock(block: BlockMessage) {
        if (expectedHash == block.hash) {
            unlock()
        } else {
            lastResponseTime = nodeClock.networkTime()
        }
    }

    private fun processing() {
        reset()
        syncStatus.setSyncStatus(PROCESSING)
    }

    private fun reset() {
        activeDelegateAddresses.clear()
        activeDelegatesLastHash.clear()
        expectedHash = EMPTY
        lastResponseTime = nodeClock.networkTime()
        sessionId = UUID.randomUUID().toString()
        unlocked = false
    }

    private fun unlock() {
        if (!unlocked) {
            syncStatus.setSyncStatus(SYNCHRONIZED)
        }

        unlocked = true
    }

}
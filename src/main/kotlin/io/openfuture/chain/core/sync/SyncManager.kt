package io.openfuture.chain.core.sync

import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.sync.*
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class SyncManager(
    private val blockService: BlockService,
    private val networkApiService: NetworkApiService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val clock: Clock,
    private val properties: NodeProperties
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncManager::class.java)
    }

    @Volatile private var responses: MutableList<SyncResponseMessage> = mutableListOf()
    private var isCheckInProgress: AtomicBoolean = AtomicBoolean()
    private var status: SyncStatus = NOT_SYNCHRONIZED
    private val selectionSize: Int = properties.getRootAddresses().size
    private val threshold: Int = selectionSize * 2 / 3
    private val lock: ReadWriteLock = ReentrantReadWriteLock()


    fun sync() {
        lock.writeLock().lock()
        try {
            if (isCheckInProgress.get()) {
                return
            }

            if (networkApiService.isChannelsEmpty()) {
                return
            }

            if (SYNCHRONIZED == status) {
                return
            }

            isCheckInProgress.set(true)
            responses.clear()
            val lastBlock = blockService.getLast()
            val message = SyncRequestMessage(clock.currentTimeMillis(), lastBlock.hash, lastBlock.height)
            networkApiService.poll(message, selectionSize)
        } finally {
            lock.writeLock().unlock()
            Thread.sleep(properties.syncResponseDelay!!)
            checkState()
        }
    }

    fun onSyncResponseMessage(msg: SyncResponseMessage) {
        lock.writeLock().lock()
        try {
            responses.add(msg)
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun checkState() {
        lock.writeLock().lock()
        try {
            if (threshold > responses.size) {
                status = NOT_SYNCHRONIZED
                return
            }

            val topList = responses.asSequence().groupBy { it.lastBlockHeight }.maxBy { it.value.size }!!.value
            val topSet = topList.asSequence().map { it.lastBlockHash }.toSet()

            if (threshold > topList.size || 1 < topSet.size) {
                status = NOT_SYNCHRONIZED
                return
            }

            if (blockService.isExists(topList.first().lastBlockHash, topList.first().lastBlockHeight)) {
                status = SYNCHRONIZED
            } else {
                status = PROCESSING
                val message = SyncBlockRequestMessage(blockService.getLast().hash)
                networkApiService.poll(message, selectionSize)
            }
        } finally {
            isCheckInProgress.set(false)
            lock.writeLock().unlock()
        }

    }

    fun onMainBlockMessage(block: MainBlockMessage) {

        mainBlockService.add(block)
    }

    fun onGenesisBlockMessage(block: GenesisBlockMessage) {
        genesisBlockService.add(block)
    }

    fun getStatus(): SyncStatus {
        lock.readLock().lock()
        try {
            return status
        } finally {
            lock.readLock().unlock()
        }

    }

    fun outOfSync() {
        lock.writeLock().lock()
        try {
            if (isCheckInProgress.get()) {
                return
            }
            status = NOT_SYNCHRONIZED
            sync()
        } finally {
            lock.writeLock().unlock()
        }

    }

}
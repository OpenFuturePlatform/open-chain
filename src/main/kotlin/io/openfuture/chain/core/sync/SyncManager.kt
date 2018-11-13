package io.openfuture.chain.core.sync

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.sync.SyncBlockRequestMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class SyncManager(
    private val blockService: BlockService,
    private val connectionService: ConnectionService,
    private val clock: Clock,
    private val properties: NodeProperties,
    private val explorerAddressesHolder: ExplorerAddressesHolder,
    private val bootstrap: Bootstrap
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncManager::class.java)
    }

    private val responses: MutableList<SyncResponseMessage> = Collections.synchronizedList(mutableListOf())
    private val selectionSize: Int = properties.getRootAddresses().size
    private val threshold: Int = selectionSize * 2 / 3
    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    @Volatile
    private var status: SyncStatus = NOT_SYNCHRONIZED

    private var channelGroup = DefaultChannelGroup("ledger-sync", GlobalEventExecutor.INSTANCE)


    fun sync() {
        lock.writeLock().lock()
        try {
            if (SYNCHRONIZED == status || PROCESSING == status) {
                return
            }

            status = PROCESSING
            responses.clear()
            val lastBlock = blockService.getLast()
            val message = SyncRequestMessage(clock.currentTimeMillis(), lastBlock.hash, lastBlock.height)
            registerNodes()
            log.error("LEDGER: channelGroup size ${channelGroup.size}")
            val groupFuture = channelGroup.writeAndFlush(message)
            log.error("LEDGER: Sync request is ${groupFuture.isSuccess}")
            log.error("LEDGER: Sync request is partial ${groupFuture.isPartialSuccess}")
//            connectionService.poll(message, selectionSize)
        } finally {
            lock.writeLock().unlock()
            Thread.sleep(properties.syncResponseDelay!!)
            checkState()
            unregisterNodes()
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

    private fun registerNodes() {
        explorerAddressesHolder.getRandomList(selectionSize).map { it.address }.forEach { address ->
            bootstrap.connect(address.host, address.port).addListener { future ->
                if (future.isSuccess) {
//                    log.error("Sync request to sent")
                    channelGroup.add((future as ChannelFuture).channel())
//                    (future as ChannelFuture).channel().writeAndFlush(msg)
                }
            }
        }
    }

    private fun unregisterNodes() {
        channelGroup.close()
        channelGroup.clear()
    }

    private fun checkState() {
        lock.writeLock().lock()
        try {
            log.error("LEDGER: <<< responses.size = ${responses.size}>>>")
            if (threshold > responses.size) {
                status = NOT_SYNCHRONIZED
                return
            }

            val topList = responses.asSequence().groupBy { it.lastBlockHeight }.maxBy { it.key }!!.value
            val topSet = topList.asSequence().map { it.lastBlockHash }.toSet()

            if ((selectionSize - 1) / 2 > topList.size || 1 < topSet.size) {
                status = NOT_SYNCHRONIZED
                return
            }

            if (blockService.isExists(topList.first().lastBlockHash, topList.first().lastBlockHeight)) {
                status = SYNCHRONIZED
                log.error("LEDGER: set SYNCHRONIZED status")
            } else {
                status = PROCESSING
                log.error("LEDGER: set PROCESSING status")
                val message = SyncBlockRequestMessage(blockService.getLast().hash)
                connectionService.poll(message, selectionSize)
            }
        } finally {
            lock.writeLock().unlock()
        }
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
            if (PROCESSING == status) {
                return
            }
            status = NOT_SYNCHRONIZED
        } finally {
            lock.writeLock().unlock()
            sync()
        }
    }


}
package io.openfuture.chain.core.sync

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.message.core.BlockMessage
import io.openfuture.chain.network.message.sync.SyncBlockDto
import io.openfuture.chain.network.message.sync.SyncBlockRequestMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

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

    @Volatile
    private var status: SyncStatus = NOT_SYNCHRONIZED

    private var chainToSync = Collections.synchronizedList(mutableListOf<SyncBlockDto>())
    private var syncResult = Collections.synchronizedSet(mutableSetOf<String>())
    private var startSyncTime = clock.currentTimeMillis()


    @Scheduled(fixedRateString = "\${node.sync-interval}")
    fun syncBlock() {
        if (explorerAddressesHolder.getNodesInfo().isEmpty()) {
            return
        }

        if (status == PROCESSING && isTimeOut()) {
            status = if (unlockIfSynchronized()) SYNCHRONIZED else NOT_SYNCHRONIZED
        }

        if (status == NOT_SYNCHRONIZED) {
            sync()
        }
    }


    @Synchronized
    fun getStatus(): SyncStatus = status

    @Synchronized
    fun outOfSync() {
        if (PROCESSING != status) {
            status = NOT_SYNCHRONIZED
            log.debug("set NOT_SYNCHRONIZED in outOfSync")
        }
        sync()
    }

    @Synchronized
    fun sync() {
        if (SYNCHRONIZED == status || PROCESSING == status) {
            log.debug("SYNCHRONIZED or PROCESSING")
            return
        }

        status = PROCESSING
        startSyncTime = clock.currentTimeMillis()
        log.debug("Set PROCESSING")
        clearData()

        val lastBlock = blockService.getLast()
        sendSyncRequest(SyncRequestMessage(clock.currentTimeMillis(), lastBlock.hash, lastBlock.height))
        Thread.sleep(properties.expiry!!)
        checkState()
    }

    fun onSyncResponseMessage(msg: SyncResponseMessage) {
        responses.add(msg)
    }

    fun onBlockMessage(msg: BlockMessage, action: BlockMessage.() -> Unit) {
        println(syncResult.toString())
        if (syncResult.add(msg.hash)) {
            action(msg)
            log.debug("BLOCK ADDED ${msg.hash}")
            unlockIfSynchronized()
        }
    }

    private fun sendSyncRequest(msg: SyncRequestMessage) {
        explorerAddressesHolder.getRandomList(selectionSize).map { it.address }.forEach { address ->
            bootstrap.connect(address.host, address.port).addListener { future ->
                if (future.isSuccess) {
                    val channel = (future as ChannelFuture).channel()
                    val res = channel.writeAndFlush(msg)
                    log.debug("Sync request sent to ${address.port}: Success is ${res.isSuccess}")
                }
            }
        }
    }


    @Synchronized
    private fun checkState() {
        log.debug("LEDGER: <<< responses.size = ${responses.size}>>>")
        if (threshold > responses.size) {
            log.debug("NOT_SYNCHRONIZED responses.size")
            status = NOT_SYNCHRONIZED
            return
        }

        if (responses.flatMap { it.blocksAfter }.isEmpty()) {
            log.debug("LEDGER: set SYNCHRONIZED status (empty)")
            status = SYNCHRONIZED
            return
        }

        if (!fillChainToSync()) {
            status = NOT_SYNCHRONIZED
            log.debug("NOT_SYNCHRONIZED fillChainToSync")
            return
        }

        log.debug("CHAIN: size=${chainToSync.size} ${chainToSync.map { it.height }.toList()}")
        val message = SyncBlockRequestMessage(blockService.getLast().hash)
        connectionService.poll(message, selectionSize)
    }

    //todo checks & simpler algorithm
    private fun fillChainToSync(): Boolean {
        val maxMatchSize = responses.groupingBy { it.blocksAfter.size }.eachCount().maxBy { it.value }!!.key
        for (i in 0 until maxMatchSize) {
            val maxEntry = responses
                .filter { it.blocksAfter.size >= maxMatchSize }
                .map { it.blocksAfter[i] }
                .groupingBy { it }.eachCount().maxBy { it.value }

            if (null == maxEntry || threshold > maxEntry.value) {
                log.debug(responses.map { it.blocksAfter.toString() }.toString())
                return false
            }

            chainToSync.add(maxEntry.key)
        }

        for (it in responses) {
            if (it.blocksAfter.getOrNull(maxMatchSize) != null) {
                chainToSync.add(it.blocksAfter[maxMatchSize])
                break
            }
        }

        return true
    }


    @Synchronized
    private fun unlockIfSynchronized(): Boolean {
        log.debug("Try to unlock")
        if (syncResult.size == chainToSync.size && syncResult == chainToSync.map { it.hash }.toSet()) {
            status = SYNCHRONIZED
            log.debug("SYNCHRONIZED")
            return true
        }
        log.debug(syncResult.toString())
        log.debug(chainToSync.toString())
        return false
    }

    private fun clearData() {
        log.debug("Clearing")
        responses.clear()
        chainToSync.clear()
        syncResult.clear()
    }

    private fun isTimeOut(): Boolean =
        clock.currentTimeMillis() - startSyncTime > properties.expiry!!

}
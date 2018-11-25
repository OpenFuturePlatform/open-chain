package io.openfuture.chain.core.sync

import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.core.BlockMessage
import io.openfuture.chain.network.message.sync.SyncBlockDto
import io.openfuture.chain.network.message.sync.SyncBlockRequestMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class SyncManager(
        private val clock: Clock,
        private val properties: NodeProperties,
        private val blockService: BlockService,
        private val networkApiService: NetworkApiService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncManager::class.java)
    }

    private var responses: ConcurrentHashMap<List<SyncBlockDto>, MutableList<NodeInfo>> = ConcurrentHashMap()
    private val selectionSize: Int = properties.getRootAddresses().size
    private val threshold: Int = selectionSize * 2 / 3

    @Volatile
    private var status: SyncStatus = NOT_SYNCHRONIZED

    private var startSyncTime = clock.currentTimeMillis()
    private var chainToSync = Collections.synchronizedList(mutableListOf<SyncBlockDto>())
    private var nodesToAsk = Collections.synchronizedList(mutableListOf<NodeInfo>())
    private var syncResult = Collections.synchronizedSet(mutableSetOf<String>())


    @Scheduled(fixedRateString = "\${node.sync-interval}")
    fun syncBlock() {
        if (status == PROCESSING && isTimeOut() && !unlockIfSynchronized()) {
            status = NOT_SYNCHRONIZED
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
        if (NOT_SYNCHRONIZED != status) {
            log.debug("SYNCHRONIZED or PROCESSING")
            return
        }

        status = PROCESSING
        startSyncTime = clock.currentTimeMillis()
        log.debug("Set PROCESSING")
        reset()

        val lastBlock = blockService.getLast()
        networkApiService.poll(SyncRequestMessage(clock.currentTimeMillis(), lastBlock.hash, lastBlock.height), selectionSize)
        Thread.sleep(properties.expiry!!)
        checkState()
    }

    fun onSyncResponseMessage(msg: SyncResponseMessage, nodeInfo: NodeInfo) {
        responses[msg.blocksAfter] = responses.getOrDefault(msg.blocksAfter, mutableListOf()).apply { add(nodeInfo) }
    }

    fun onBlockMessage(msg: BlockMessage, action: BlockMessage.() -> Unit) {
        action(msg)
        if (syncResult.add(msg.hash)) {
            log.debug("BLOCK ADDED ${msg.hash}")
            unlockIfSynchronized()
        }
    }

    @Synchronized
    private fun checkState() {
        log.debug("LEDGER: <<< responses.size = ${responses.flatMap { it.value }.size}>>>")
        if (threshold > responses.flatMap { it.value }.size) {
            log.debug("~~~~~~~~~~~~~NOT_SYNCHRONIZED responses.size~~~~~~~~~~~~~")
            status = NOT_SYNCHRONIZED
            return
        }

        if (responses.flatMap { it.key }.isEmpty()) {
            log.debug("SYNCHRONIZED status (empty)")
            status = SYNCHRONIZED
            return
        }

        if (!fillChainToSync()) {
            status = NOT_SYNCHRONIZED
            log.debug("~~~~~~~~~~~~~NOT_SYNCHRONIZED fillChainToSync~~~~~~~~~~~~~")
            responses.entries.forEach {
                log.debug("${it.key.map { it.height }}:${it.value.size}")
            }
            return
        }

        val nodeToAsk = nodesToAsk.first()

        log.debug("CHAIN: size=${chainToSync.size} ${chainToSync.map { it.height }.toList()}")
        log.debug("Node to ask = ${nodeToAsk.address}")
        networkApiService.sendToAddress(SyncBlockRequestMessage(blockService.getLast().hash), nodeToAsk)
    }

    private fun findSubCommonChainAnswer(firstPopular: Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>): Boolean {

        var diff: Int = 0
        val secondPopular = responses.filter { it.key != firstPopular.key }.maxBy { it.value.size }!!

        val maxHeightSeq = firstPopular.key.first().height
        val maxHeightSeq2 = secondPopular.key.first().height

        if (maxHeightSeq > maxHeightSeq2) {
            diff = (maxHeightSeq - maxHeightSeq2).toInt()
            if (firstPopular.key.drop(diff) == secondPopular.key.dropLast(diff) && isMoreThreshold(firstPopular, secondPopular)) {
                nodesToAsk.addAll(firstPopular.value)
                chainToSync.addAll(firstPopular.key)
                return true
            }
        } else if (maxHeightSeq < maxHeightSeq2) {
            diff = (maxHeightSeq2 - maxHeightSeq).toInt()
            if (secondPopular.key.drop(diff) == firstPopular.key.dropLast(diff) && isMoreThreshold(firstPopular, secondPopular)) {
                nodesToAsk.addAll(firstPopular.value)
                chainToSync.addAll(firstPopular.key)
                return true
            }
        }
        return false
    }

    fun isMoreThreshold(first: Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>, second: Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>): Boolean = threshold <= first.value.size + second.value.size

    //todo checks & simpler algorithm
    private fun fillChainToSync(): Boolean {
        val maxMatch = responses.maxBy { it.value.size }!!
        val maxBlockSize = responses.maxBy { it.key.size }!!

        // if max length chain is most often
        if (responses.keys.size == 1 && maxMatch.value.size >= threshold) {
            nodesToAsk.addAll(maxMatch.value)
            chainToSync.addAll(maxMatch.key)
            return true
        }

        if (responses.filter { it.key.size == 30 }.size == responses.keys.size) {
            return findSubCommonChainAnswer(maxMatch)
        }


        val withoutMaxBlockSize = responses.filter { it.key != maxBlockSize.key }
        log.debug("full = ${responses.keys.size} after filter = ${withoutMaxBlockSize.keys.size}")
        val nextMaxBlockSize = withoutMaxBlockSize.maxBy { it.key.size }!!

        val diff = maxBlockSize.key.size - nextMaxBlockSize.key.size

        if (maxBlockSize.key.drop(diff) == nextMaxBlockSize.key && isMoreThreshold(maxBlockSize, nextMaxBlockSize)) {
            nodesToAsk.addAll(maxBlockSize.value)
            chainToSync.addAll(maxMatch.key)
            return true
        }

        if (threshold > maxMatch.value.size + responses.filter { it.key.isEmpty() }.values.size) {
            return false
        }

        chainToSync.addAll(maxMatch.key)
        nodesToAsk.addAll(maxMatch.value)
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

        return false
    }

    private fun reset() {
        log.debug("RESET")
        responses.clear()
        chainToSync.clear()
        syncResult.clear()
        nodesToAsk.clear()
    }

    //todo need to think
    private fun isTimeOut(): Boolean = clock.currentTimeMillis() - startSyncTime > properties.syncInterval!!

}
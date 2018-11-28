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
        networkApiService.poll(
            SyncRequestMessage(clock.currentTimeMillis(), lastBlock.hash, lastBlock.height),
            selectionSize
        )
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


        log.debug("CHAIN: size=${chainToSync.size} ${chainToSync.map { it.height }.toList()}")
        nodesToAsk.forEach { networkApiService.sendToAddress(SyncBlockRequestMessage(blockService.getLast().hash), it) }
    }

    //todo checks & simpler algorithm
    private fun fillChainToSync(): Boolean {

        val maxPopular = maxPopularChain(responses)

        val maxByHeight = responses.filter { it.key.isNotEmpty() }.maxBy { it.key.first().height }!!

        if (maxByHeight.key == maxPopular.key) {
            if (maxPopular.value.size >= threshold) {
                addNodeForSync(maxPopular.value, maxPopular.key)
                return true
            } else if (maxPopular.key.size > 1) {
                val subPopChain = maxPopular.key.drop(1)
                val subChain = responses.filter { it.key.take(subPopChain.size) == subPopChain }
                if (maxPopular.value.size + subChain.values.first().size >= threshold) {
                    nodesToAsk.addAll(subChain.values.first())
                    addNodeForSync(maxPopular.value, maxPopular.key)
                    return true
                }
            }

        } else {
            val nextHeight =
                responses.filter { it.key.isNotEmpty() && it.key.first().height - 1 == maxPopular.key.first().height }
            if (nextHeight.isNotEmpty() && dropChain(nextHeight.keys.first(), maxPopular.key)) {
                nodesToAsk.addAll(nextHeight.values.first())
                addNodeForSync(maxPopular.value, maxPopular.key)
                return true
            }
        }
        val emptyAnswer = responses.filter { it.key.isEmpty() }
        if (maxPopular.key.size == 1 && emptyAnswer.isNotEmpty() && maxPopular.value.size + emptyAnswer.values.first().size >= threshold) {
            nodesToAsk.addAll(emptyAnswer.values.first())
            addNodeForSync(maxPopular.value, maxPopular.key)
            return true
        }

        return false
    }

    fun maxPopularChain(response: ConcurrentHashMap<List<SyncBlockDto>, MutableList<NodeInfo>>): Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>> {
        val maxCountResponse = response.maxBy { it.value.size }!!.value.size
        return response.filter { it.key.isNotEmpty() && it.value.size == maxCountResponse }.maxBy {
            it.key.first().height
        }!!
    }

    private fun dropChain(chain: List<SyncBlockDto>, maxPop: List<SyncBlockDto>): Boolean {
        val chainDropped = chain.drop(1)
        return chainDropped == maxPop.take(chainDropped.size)
    }


    private fun addNodeForSync(value: MutableList<NodeInfo>, key: List<SyncBlockDto>) {
        nodesToAsk.addAll(value)
        chainToSync.addAll(key)
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
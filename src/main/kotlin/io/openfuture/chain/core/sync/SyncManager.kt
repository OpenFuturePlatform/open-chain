package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
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

    private var lastBlockToSync: SyncBlockDto? = null


    @Scheduled(fixedRateString = "\${node.sync-interval}")
    fun syncBlock() {
        if (status == PROCESSING && isTimeOut()) {
            status = NOT_SYNCHRONIZED
        }

        if (status == NOT_SYNCHRONIZED) {
            sync()
        }
    }

    fun getStatus(): SyncStatus = status

    @Synchronized
    fun outOfSync() {
        if (PROCESSING != status) {
            status = NOT_SYNCHRONIZED
            log.debug("set NOT_SYNCHRONIZED in outOfSync")
        }
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
        checkState(lastBlock)
    }

    fun onSyncResponseMessage(msg: SyncResponseMessage, nodeInfo: NodeInfo) {
        responses[msg.blocksAfter] = responses.getOrDefault(msg.blocksAfter, mutableListOf()).apply { add(nodeInfo) }
    }

    fun onBlockMessage(msg: BlockMessage, action: BlockMessage.() -> Unit) {
        action(msg)
        log.debug("BLOCK ADDED ${msg.hash}")
        if (lastBlockToSync != null && lastBlockToSync == SyncBlockDto(msg.height, msg.hash)) {
            status = SYNCHRONIZED
            log.debug("SYNCHRONIZED")
        }
    }

    private fun checkState(lastBlock: Block) {
        log.debug("LEDGER: <<< responses.size = ${responses.flatMap { it.value }.size} >>>")
        if (threshold > responses.flatMap { it.value }.size) {
            log.debug("LEDGER: NOT_SYNCHRONIZED reason - responses size")
            status = NOT_SYNCHRONIZED
            return
        }

        if (responses.flatMap { it.key }.isEmpty()) {
            log.debug("LEDGER: SYNCHRONIZED (empty)")
            status = SYNCHRONIZED
            return
        }

        val nodesToAsk = fillChainToSync(lastBlock)
        if (nodesToAsk.isEmpty()) {
            status = NOT_SYNCHRONIZED
            log.debug("LEDGER: NOT_SYNCHRONIZED reason - chain not found")
            responses.entries.forEach { log.debug("${it.key.map { it.height }}:${it.value.size}") }
            return
        }


        log.debug("LEDGER: last block to sync: $lastBlockToSync")
        nodesToAsk.forEach { networkApiService.sendToAddress(SyncBlockRequestMessage(lastBlock.hash), it) }
    }


    fun fillChainToSync(lastBlock: Block): List<NodeInfo> {
        val maxPopular = maxPopularChain(responses)
        val maxByHeight = responses.maxBy { it.chainHeight() }!!

        // If most frequent chain is the longest
        if (maxByHeight.chain == maxPopular.chain) {
            if (isEnough(maxPopular.answersCount())) {
                return returnResult(maxByHeight.chain.first(), maxPopular.answers)
            }

            if (maxPopular.chain.size == 1) {
                val emptyAnswer = responses.firstOrNull { it.chain.isEmpty() } ?: return emptyList()
                if (isEnough(maxPopular.answersCount() + emptyAnswer.answersCount())) {
                    return returnResult(maxByHeight.chain.first(), maxPopular.answers, emptyAnswer.answers)
                }
            } else {
                val subPopChain = maxPopular.chain.drop(1)
                val subChain = responses.firstOrNull { it.chain.take(subPopChain.size) == subPopChain }
                    ?: return emptyList()
                if (isEnough(maxPopular.answersCount() + subChain.answersCount())) {
                    return returnResult(maxByHeight.chain.first(), maxPopular.answers, subChain.answers)
                }
            }

        }

        //if most frequent chain is a subchain of a larger chain
        if (maxPopular.chain.isEmpty()) {
            val oneElementChain =
                responses.firstOrNull { it.chain.size == 1 && it.chainHeight() isNext lastBlock.height }
                    ?: return emptyList()
            if (isEnough(maxPopular.answersCount() + oneElementChain.answersCount())) {
                return returnResult(oneElementChain.chain.first(), oneElementChain.answers, maxPopular.answers)
            }
        } else {
            val nextHeight = responses.firstOrNull { it.chainHeight() isNext maxPopular.chainHeight() }
                ?: return emptyList()

            val subChain = nextHeight.chain.drop(1)
            if (subChain == maxPopular.chain.take(subChain.size)) {
                return returnResult(nextHeight.chain.first(), nextHeight.answers, maxPopular.answers)
            }
        }

        return emptyList()
    }

    private fun isEnough(available: Int): Boolean = available > threshold

    private fun maxPopularChain(response: ConcurrentHashMap<List<SyncBlockDto>, MutableList<NodeInfo>>): Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>> =
        response.maxWith(Comparator { r1, r2 ->
            return@Comparator when {
                r1.answersCount() == r2.answersCount() -> return@Comparator (r1.chainHeight() - r2.chainHeight()).toInt()
                else -> r1.answersCount() - r2.answersCount()
            }
        })!!

    private fun returnResult(lastChainBlock: SyncBlockDto, vararg answers: List<NodeInfo>): MutableList<NodeInfo> {
        lastBlockToSync = lastChainBlock
        val nodeToSync: MutableList<NodeInfo> = mutableListOf()
        nodeToSync.addAll(answers.flatMap { it })
        return nodeToSync
    }

    private fun reset() {
        log.debug("RESET")
        responses.clear()
        lastBlockToSync = null
    }

    //todo need to think
    private fun isTimeOut(): Boolean = clock.currentTimeMillis() - startSyncTime > properties.syncExpiry!!

    private fun <K, V> Map<out K, V>.firstOrNull(predicate: (Map.Entry<K, V>) -> Boolean): Map.Entry<K, V>? =
        this.filter(predicate).entries.firstOrNull()

    private fun Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>.answersCount(): Int = this.value.size

    private fun Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>.chainHeight(): Long = this.key.firstOrNull()?.height
        ?: 0

    private val Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>.chain: List<SyncBlockDto>
        get() = this.key

    private val Map.Entry<List<SyncBlockDto>, MutableList<NodeInfo>>.answers: MutableList<NodeInfo>
        get() = this.value

    private infix fun Long.isNext(prev: Long): Boolean = this == prev + 1

}
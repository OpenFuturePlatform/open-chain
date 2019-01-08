package io.openfuture.chain.core.sync

import io.openfuture.chain.core.component.SyncFetchBlockScheduler
import io.openfuture.chain.core.exception.ChainOutOfSyncException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ChainSynchronizer(
    private val blockService: BlockService,
    private val syncFetchBlockScheduler: SyncFetchBlockScheduler,
    private val delegateService: DelegateService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService,
    private val rewardTransactionService: RewardTransactionService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED

    private lateinit var syncSession: SyncSession

    private lateinit var listNodeInfo: List<NodeInfo>

    fun getStatus(): SyncStatus = status

    @Synchronized
    fun sync() {
        log.debug("Chain in status=$status")
        if (PROCESSING == status) {
            return
        }
        status = PROCESSING

        startSynchronization()
    }

    fun latestGenesisBlockResponse(receivedLastGenesisBlock: GenesisBlockMessage) {
        syncFetchBlockScheduler.deactivate()

        val latestGenesisBlock = getGenesisBlockFromMessage(receivedLastGenesisBlock)
        val currentGenesisBlock = genesisBlockService.getLast()

        if (currentGenesisBlock.hash == latestGenesisBlock.hash) {
            syncSession = SyncCurrentEpochSession(currentGenesisBlock)
            fetchEpoch(getEpochIndex(), listNodeInfo)
            return
        }

        syncSession = DefaultSyncSession(latestGenesisBlock, currentGenesisBlock)
        listNodeInfo = latestGenesisBlock.payload.activeDelegates.map { getNodeInfo(it) }

        fetchEpoch(getEpochIndex() - 1, listNodeInfo)
    }

    fun epochResponse(address: InetAddress, msg: EpochResponseMessage) {
        syncFetchBlockScheduler.deactivate()

        if (!msg.isEpochExists) {
            fetchEpoch(getEpochIndex(), listNodeInfo.filter { it.uid != msg.nodeId })
            return
        }

        val genesisBlock = getGenesisBlockFromMessage(msg.genesisBlock!!)
        val listBlocks: MutableList<Block> = mutableListOf()

        listBlocks.addAll(msg.mainBlocks.map {
            val mainBlock = MainBlock.of(it)
            mainBlock.payload.rewardTransaction = mutableListOf(RewardTransaction.of(it.rewardTransaction, mainBlock))
            mainBlock
        })

        if (syncSession is SyncCurrentEpochSession) {

            if (!syncSession.add(listBlocks)) {
                fetchEpoch(getEpochIndex(), listNodeInfo.filter { it.uid != msg.nodeId })
            } else {
                saveBlocks()
            }
            return
        }

        listBlocks.add(genesisBlock)

        if (!syncSession.add(listBlocks)) {
            fetchEpoch(getEpochIndex(), listNodeInfo.filter { it.uid != msg.nodeId })
            return
        }

        if (!syncSession.isComplete()) {
            fetchEpoch(getEpochIndex() - 1, listNodeInfo)
            return
        }

        saveBlocks()
    }

    fun checkSync(block: Block) {
        val lastBlock = blockService.getLast()
        if (!isValidHeight(block, lastBlock) || !isValidPreviousHash(block, lastBlock)) {
            sync()
            throw ChainOutOfSyncException()
        }
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun getEpochIndex(): Long = (syncSession.getLastBlock() as GenesisBlock).payload.epochIndex

    @Synchronized
    private fun setSynchronized() {
        status = SYNCHRONIZED
        log.debug("Chain in status=$status")
    }

    @Synchronized
    private fun setNotSynchronized() {
        status = NOT_SYNCHRONIZED
        log.debug("Set status=$status")
    }


    private fun startSynchronization() {
        listNodeInfo = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()

        fetchLatestGenesisBlock(listNodeInfo)
    }

    private fun fetchLatestGenesisBlock(listNodeInfo: List<NodeInfo>) {
        val message = SyncRequestMessage()

        networkApiService.sendToAddress(message, listNodeInfo.shuffled().first())
        syncFetchBlockScheduler.activate(message, listNodeInfo)
    }

    private fun fetchEpoch(epochIndex: Long, listNodeInfo: List<NodeInfo>) {
        val message = EpochRequestMessage(epochIndex)

        networkApiService.sendToAddress(message, listNodeInfo.shuffled().first())
        syncFetchBlockScheduler.activate(message, listNodeInfo)
    }

    private fun getNodeInfo(delegate: Delegate): NodeInfo = NodeInfo(delegate.nodeId, NetworkAddress(delegate.host, delegate.port))

    private fun getGenesisBlockFromMessage(message: GenesisBlockMessage): GenesisBlock {
        val delegates = message.delegates.asSequence().map { delegateService.getByPublicKey(it) }.toMutableList()
        return GenesisBlock.of(message, delegates)
    }

    private fun saveBlocks() {
        try {
            val currentLastBlock = blockService.getLast()
            val existedBlock = syncSession.getStorage().find { it.hash == currentLastBlock.hash }

            val filteredStorage = existedBlock?.let {
                val nextIndex = syncSession.getStorage().indexOf(existedBlock) + 1
                syncSession.getStorage().subList(nextIndex, syncSession.getStorage().size)
            } ?: syncSession.getStorage()

            filteredStorage.forEach {
                if (it is MainBlock) {
                    val rewardTransaction = it.payload.rewardTransaction.first()
                    it.payload.rewardTransaction.clear()
                    blockService.save(it)
                    rewardTransaction.block = it
                    rewardTransactionService.save(rewardTransaction)
                } else {
                    blockService.save(it)
                }
            }
            setSynchronized()
        } catch (e: Exception) {
            log.error("Save block is failed: $e")
            setNotSynchronized()
        }

    }
}
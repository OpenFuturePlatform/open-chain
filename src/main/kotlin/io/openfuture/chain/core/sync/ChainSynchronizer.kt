package io.openfuture.chain.core.sync

import io.openfuture.chain.core.component.SyncFetchBlockScheduler
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.sync.SyncStatus.PROCESSING
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ChainSynchronizer(
    private val blockService: BlockService,
    private val properties: NodeProperties,
    private val syncFetchBlockScheduler: SyncFetchBlockScheduler,
    private val delegateService: DelegateService,
    private val networkApiService: NetworkApiService,
    private val genesisBlockService: GenesisBlockService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED

    fun getStatus(): SyncStatus = status

    private lateinit var syncSession: SyncSession

    private lateinit var listNodeInfo: List<NodeInfo>


    @Synchronized
    fun sync() {
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
            fetchEpoch(getEpochIndex(), listNodeInfo)
            syncSession = SyncCurrentEpochSession(currentGenesisBlock)
            return
        }

        syncSession = DefaultSyncSession(latestGenesisBlock, currentGenesisBlock)

        listNodeInfo = latestGenesisBlock.payload.activeDelegates.map { getNodeInfo(it) }

        fetchEpoch(getEpochIndex() - 1, listNodeInfo)
    }


    fun epochResponse(address: InetAddress, msg: EpochResponseMessage) {
        syncFetchBlockScheduler.deactivate()

        val genesisBlock = getGenesisBlockFromMessage(msg.genesisBlock!!)
        val listBlocks: MutableList<Block> = mutableListOf()

        listBlocks.addAll(msg.mainBlocks.map { MainBlock.of(it) })

        if (syncSession is SyncCurrentEpochSession) {

            if (!syncSession.add(listBlocks)) {
                fetchEpoch(getEpochIndex(), listNodeInfo.filter { it.uid != msg.nodeId })
            } else {
                syncSession.getStorage().forEach { blockService.save(it) }

//            SET_SYNC?
                setSynchronized()
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

        syncSession.getStorage().forEach { blockService.save(it) }

//            SET_SYNC?
        setSynchronized()
    }

    private fun getEpochIndex(): Long {
        return (syncSession.getStorage().last() as GenesisBlock).payload.epochIndex
    }

    @Synchronized
    private fun setSynchronized() {
        status = SYNCHRONIZED
    }

    private fun startSynchronization() {
        listNodeInfo = genesisBlockService.getLast().payload.activeDelegates.map { getNodeInfo(it) }.toList()

        fetchLatestGenesisBlock(listNodeInfo)
    }

    private fun fetchLatestGenesisBlock(listNodeInfo: List<NodeInfo>) {
        val message = SyncRequestMessage()

        networkApiService.sendToAddress(message, listNodeInfo.first())
        syncFetchBlockScheduler.activate(message, listNodeInfo, properties.syncExpiry!!)
    }

    private fun fetchEpoch(epochIndex: Long, listNodeInfo: List<NodeInfo>) {
        val message = EpochRequestMessage(epochIndex)

        networkApiService.sendToAddress(message, listNodeInfo.first())
        syncFetchBlockScheduler.activate(message, listNodeInfo, properties.syncExpiry!!)
    }

    private fun getNodeInfo(delegate: Delegate): NodeInfo = NodeInfo(delegate.nodeId, NetworkAddress(delegate.host, delegate.port))

    private fun getGenesisBlockFromMessage(message: GenesisBlockMessage): GenesisBlock {
        val delegates = message.delegates.asSequence().map { delegateService.getByPublicKey(it) }.toMutableList()
        return GenesisBlock.of(message, delegates)
    }

}
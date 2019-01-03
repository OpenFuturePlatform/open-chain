package io.openfuture.chain.core.sync

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncStatus.PROCESSING
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.component.AddressesHolder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.sync.EpochRequestMessage
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.serialization.Serializable
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ChainSynchronizer(
    private val clock: Clock,
    private val blockService: BlockService,
    private val properties: NodeProperties,
    private val addressesHolder: AddressesHolder,
    private val delegateService: DelegateService,
    private val networkApiService: NetworkApiService,
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ChainSynchronizer::class.java)
    }

    @Volatile
    private var status: SyncStatus = SYNCHRONIZED

    fun getStatus(): SyncStatus = status


//    @Scheduled(fixedRateString = "\${node.sync-interval}")
//    fun syncBlock() {
//        if (status != SYNCHRONIZED) {
//            log.debug("Ledger in $status")
//            sync()
//        }
//    }


    @Synchronized
    fun sync(publicKey: String) {

        if (PROCESSING == status) {
            return
        }

        status = PROCESSING

        delegateService.getByPublicKey(publicKey)
//        if (NOT_SYNCHRONIZED == status && null == latestGenesisBlock) {
//            status = PROCESSING
//            networkApiService.sendToAddress(SyncRequestMessage(), activeDelegateNodeInfo!!)
//            return
//        }

        if (isLongAnswer(lastResponseTime)) {
            when {
                null == cursorGenesisBlock -> {
                    status = PROCESSING
                    networkApiService.sendToAddress(SyncRequestMessage(), activeDelegateNodeInfo!!)
                }
                isNotLatestGenesisHash() -> sendMessageToRandomDelegate(
                    EpochRequestMessage(cursorGenesisBlock!!.payload.epochIndex - 1), delegateNodeInfo)
                else -> resetCursorAndLastBlock()
            }
            return
        }
    }

    fun setReceivedLastGenesisBlock(receivedLastGenesisBlock: GenesisBlockMessage) {
        val delegates = receivedLastGenesisBlock.delegates.asSequence().map { delegateService.getByPublicKey(it) }.toMutableList()
        val block = GenesisBlock.of(receivedLastGenesisBlock, delegates)

        if (block.hash == currentLastGenesisBlock.hash) {
            latestGenesisBlock = block
            cursorGenesisBlock = block
            val nodesInfo = currentLastGenesisBlock.payload
                .activeDelegates.map { NodeInfo(it.nodeId, NetworkAddress(it.host, it.port)) }
            sendMessageToRandomDelegate(EpochRequestMessage(block.payload.epochIndex), nodesInfo)
            return
        }

        if (latestGenesisBlock == null) {
            cursorGenesisBlock = block
            latestGenesisBlock = block
            delegateNodeInfo = block.payload
                .activeDelegates.map { NodeInfo(it.nodeId, NetworkAddress(it.host, it.port)) }

            blockService.saveUnique(block)
            sendMessageToRandomDelegate(EpochRequestMessage(block.payload.epochIndex - 1), delegateNodeInfo)
        }
    }

    fun epochResponse(address: InetAddress, msg: EpochResponseMessage) {
        latestGenesisBlock ?: return

        lastResponseTime = clock.currentTimeMillis() + properties.expiry!!

        if (!msg.isEpochExists) {
            sendEpochRequestToFilteredNodes(cursorGenesisBlock!!.payload.epochIndex, address, msg.nodeId)
            return
        }

        val delegates = msg.genesisBlock!!.delegates.asSequence().map { delegateService.getByPublicKey(it) }.toMutableList()

        val genesisBlockMessage = msg.genesisBlock!!
        val genesisBlock = GenesisBlock.of(genesisBlockMessage, delegates)
        val mainBlockMessages = msg.mainBlocks

        if (mainBlockMessages.isEmpty() && isLatestEpochForSync(genesisBlockMessage.hash, address, msg.nodeId)) {
            setSynchronized()
            return
        }

        epochSync(msg, genesisBlock, address)
    }

    private fun epochSync(msg: EpochResponseMessage, genesisBlock: GenesisBlock, address: InetAddress) {
        val mainBlockMessages = msg.mainBlocks
        val mainBlocks = mainBlockMessages.map { MainBlock.of(it) }

        if (mainBlocks.size == consensusProperties.epochHeight!!) {
            if (!isValidBlocks(genesisBlock, mainBlocks, cursorGenesisBlock!!)) {
                sendEpochRequestToFilteredNodes(genesisBlock.payload.epochIndex, address, msg.nodeId)
                return
            }
            mainBlockService.saveUniqueBlocks(mainBlocks)
            blockService.saveUnique(genesisBlock)

            if (currentLastGenesisBlock.hash != genesisBlock.hash) {
                cursorGenesisBlock = genesisBlock
                sendMessageToRandomDelegate(EpochRequestMessage(cursorGenesisBlock!!.payload.epochIndex - 1), delegateNodeInfo)
            } else {
                currentLastGenesisBlock = latestGenesisBlock!!
                setSynchronized()
            }
        } else if (!isItActiveDelegate(address, msg.nodeId)) {
            sendEpochRequestToFilteredNodes(genesisBlock.payload.epochIndex, address, msg.nodeId)
        } else if (isLatestEpochForSync(genesisBlock.hash, address, msg.nodeId) && isValidBlocks(genesisBlock, mainBlocks)) {
            mainBlockService.saveUniqueBlocks(mainBlocks)
            setSynchronized()
        }
    }

    @Synchronized
    private fun setSynchronized() {
        resetCursorAndLastBlock()
        activeDelegateNodeInfo = null
        status = SYNCHRONIZED
        lastResponseTime = 0L
    }

    private fun getFilteredNodesInfo(address: InetAddress, nodeId: String): List<NodeInfo> =
        delegateNodeInfo.filter { it.address.host != address.hostAddress && it.uid != nodeId }

    private fun sendEpochRequestToFilteredNodes(epochIndex: Long, address: InetAddress, nodeId: String) {
        val nodesInfo = getFilteredNodesInfo(address, nodeId)
        sendMessageToRandomDelegate(EpochRequestMessage(epochIndex), nodesInfo)
    }

    private fun sendMessageToRandomDelegate(message: Serializable, nodesInfo: List<NodeInfo>): Boolean {
        if (nodesInfo.isEmpty()) return false

        lastResponseTime = clock.currentTimeMillis() + properties.syncExpiry!!

        val shuffledNodesInfo = nodesInfo.shuffled()
        for (nodeInfo in shuffledNodesInfo) {
            if (networkApiService.sendToAddress(message, nodeInfo)) {
                return true
            }
        }
        return false
    }

    private fun isLongAnswer(lastResponseTime: Long): Boolean = status == PROCESSING && clock.currentTimeMillis() >= lastResponseTime

    private fun isNotLatestGenesisHash(): Boolean = currentLastGenesisBlock.hash != cursorGenesisBlock!!.hash

    private fun isItActiveDelegate(address: InetAddress, nodeId: String): Boolean =
        latestGenesisBlock!!.payload.activeDelegates.any { it.host == address.hostAddress && it.nodeId == nodeId }

    private fun isLatestEpochForSync(receivedHash: String, address: InetAddress, nodeId: String): Boolean =
        receivedHash == currentLastGenesisBlock.hash && isItActiveDelegate(address, nodeId)

    private fun isValidBlocks(genesisBlock: GenesisBlock, mainBlocks: List<MainBlock>): Boolean {
        val blocks = ArrayList<Block>(mainBlocks.size + 1)
        blocks.add(genesisBlock)
        blocks.addAll(mainBlocks)
        val blockIterator = blocks.iterator()
        return isValidBlocks(blockIterator, true)
    }

    private fun isValidBlocks(
        genesisBlock: GenesisBlock,
        mainBlocks: List<MainBlock>,
        lastGenesisBlock: GenesisBlock
    ): Boolean {
        val blocks = ArrayList<Block>(mainBlocks.size + 2)
        blocks.add(genesisBlock)
        blocks.addAll(mainBlocks)
        blocks.add(lastGenesisBlock)
        val blockIterator = blocks.reversed().iterator()
        return isValidBlocks(blockIterator, false)
    }

    private fun isValidBlocks(blockIterator: Iterator<Block>, isStraight: Boolean): Boolean {
        var currentMainBlock = blockIterator.next()
        while (blockIterator.hasNext()) {
            if (latestGenesisBlock!!.height == currentMainBlock.height + 1) {
                isPreviousBlockValid(latestGenesisBlock, currentMainBlock)
            }
            val nextBlock = blockIterator.next()
            if (!isPreviousBlockValid(nextBlock, currentMainBlock, isStraight)) {
                return false
            }
            currentMainBlock = nextBlock
        }
        return true
    }

    private fun isPreviousBlockValid(nextBlock: Block, currentBlock: Block, isStraight: Boolean): Boolean {
        return if (isStraight) {
            mainBlockService.isPreviousBlockValid(currentBlock, nextBlock)
        } else {
            mainBlockService.isPreviousBlockValid(nextBlock, currentBlock)
        }
    }

}
package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import org.springframework.stereotype.Component

@Component
class DBChecker(
    private val blockManager: BlockManager,
    private val transactionManager: TransactionManager,
    private val stateManager: StateManager,
    private val receiptService: ReceiptService,
    private val consensusProperties: ConsensusProperties
) {

    fun prepareDB(syncMode: SyncMode) {
        when (syncMode) {
            FULL -> {
                val lastBlock = blockManager.getLast()
                val lastGenesisBlock = lastBlock as? GenesisBlock
                    ?: blockManager.getPreviousGenesisBlockByHeight(lastBlock.height)
                val lastEpochIndex = lastGenesisBlock.getPayload().epochIndex

                val lastValidBlockHeight = lastValidBlockHeightByFullMode(lastEpochIndex)

                val failBlockHeight = lastValidBlockHeight + 1L
                if (failBlockHeight <= lastBlock.height) {
                    val range = LongRange(failBlockHeight, lastBlock.height)
                    blockManager.deleteByHeightIn(range.toList())
                }
            }
            LIGHT -> {}
        }
    }

    private fun lastValidBlockHeightByFullMode(lastEpochIndex: Long): Long {
        val epochHeight = consensusProperties.epochHeight!!
        var lastValidBlockHeight = 1L
        loop@ for (epochIndex in 1L..lastEpochIndex) {
            val genesisBlock = blockManager.findGenesisBlockByEpochIndex(epochIndex)!!

            var previousBlock: Block = genesisBlock
            val blocks = blockManager.getMainBlocksByEpochIndex(epochIndex)

            if (blocks.isEmpty()) continue

            for (index in 1..blocks.size) {
                lastValidBlockHeight = ((epochIndex - 1L) * epochHeight) + index

                val block = blocks[index - 1]
                if (!blockManager.verify(block, previousBlock, false)) {
                    break@loop
                }

                previousBlock = block
            }
        }

        return lastValidBlockHeight
    }

//    private fun deleteInvalidChainPart(height: Long, heightTo: Long) {
//        val heightsForDelete = LongRange(height + 1, heightTo).toList()
//        blockManager.deleteByHeightIn(heightsForDelete)
//    }
//
//    private fun lastValidBlockHeight(syncMode: SyncMode): Long {
//        val epochHeight = consensusProperties.epochHeight!! + 1L
//        var indexFrom = 1L
//        var indexTo = indexFrom + epochHeight
//        var heights = (indexFrom..indexTo).toList()
//        var blocks = blockManager.getAllByHeightIn(heights).toMutableList()
//        var result = blocks.first()
//        val lastChainBlock = blockManager.getLast()
//        while (!blocks.isEmpty()) {
//            result = validateEpoch(blocks, syncMode) ?: result
//            if (result != blocks.last()) {
//                break
//            }
//            indexFrom += indexTo
//            indexTo += epochHeight
//            heights = (indexFrom..indexTo).toList()
//            blocks = blockManager.getAllByHeightIn(heights).toMutableList()
//        }
//
//        return if (!isValidBlock(lastChainBlock, syncMode)) {
//            result.height - 1
//        } else {
//            result.height
//        }
//    }
//
//    private fun validateEpoch(blocks: List<Block>, syncMode: SyncMode): Block? {
//        var result: Block? = null
//        for (i in blocks.indices) {
//
//            if (i == blocks.lastIndex) {
//                continue
//            }
//
//            val current = blocks[i]
//            if (!isValidBlock(current, syncMode)) {
//                return result
//            }
//
//            val next = blocks[i + 1]
//            result = current
//
//            if (!isValidBlocksHashes(current, next)) {
//                return result
//            }
//        }
//        return result
//    }
//
//    private fun isValidBlock(block: Block, syncMode: SyncMode): Boolean {
//        if (!isValidBlockState(block)) {
//            return false
//        }
//        if (block.hash != ByteUtils.toHexString(HashUtils.doubleSha256(block.getBytes()))) {
//            return false
//        }
//        if (FULL == syncMode) {
//            return isValidTransactions(block) && isValidBlockReceipts(block)
//        }
//        if (LIGHT == syncMode && block is MainBlock) {
//            val delegateTxCount = transactionManager.getCountDelegateTransactionsByBlock(block)
//            val transferTxCount = transactionManager.getCountTransferTransactionsByBlock(block)
//            val voteTxCount = transactionManager.getCountVoteTransactionsByBlock(block)
//            return (delegateTxCount + transferTxCount + voteTxCount) == 0L
//        }
//        return true
//    }

}
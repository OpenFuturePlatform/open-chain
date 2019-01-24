package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import io.openfuture.chain.network.property.NodeProperties
import org.flywaydb.core.api.callback.Callback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.springframework.stereotype.Component

@Component
class FlywayDBChecker(
    private val nodeProperties: NodeProperties,
    private val consensusProperties: ConsensusProperties,
    private val blockService: BlockService,
    private val fullSyncCursor: FullSyncCursor,
    private val chainSynchronizer: ChainSynchronizer
) : Callback {

    override fun handle(event: Event?, context: Context?) {
        if (!isValidkDb(nodeProperties.syncMode!!)) {
            chainSynchronizer.sync()
        }
    }

    override fun canHandleInTransaction(event: Event?, context: Context?): Boolean = true

    override fun supports(event: Event?, context: Context?): Boolean = (Event.AFTER_MIGRATE == event)

    private fun isValidkDb(syncMode: SyncMode): Boolean {
        val epochHeight = consensusProperties.epochHeight!!
        fullSyncCursor.cursor = 1L
        var indexFrom = fullSyncCursor.cursor
        var indexTo = indexFrom!! + epochHeight
        var block: Block? = null
        do {
            val blocks = blockService.findAllByHeightBetween(indexFrom, indexTo)
            indexFrom += epochHeight + 1
            indexTo += epochHeight + 1

            val blocksIterator = blocks.iterator()
            if (null == block) {
                block = blocksIterator.next()
            }
            while (blocksIterator.hasNext()) {
                val nextBlock = blocksIterator.next()
                when (syncMode) {
                    LIGHT -> {
                        if (!isValidLightBlocks(block!!, nextBlock)) {
                            return false
                        }
                    }
                    FULL -> {
                        if (!isValidFullBlocks(block!!, nextBlock)) {
                            return false
                        }
                    }
                }

                block = nextBlock
            }
        } while (!blocks.isEmpty())
        if (!isValidBlockState(block!!)) {
            return false
        }
        if (FULL == syncMode) {
            fullSyncCursor.cursor = block.height
        }
        return true
    }

    private fun isValidFullBlocks(block: Block, nextBlock: Block): Boolean {
        if (!isValidBlocksHashes(block, nextBlock) || !isValidBlockState(block)) {
            return false
        }

        if (block is MainBlock) {
            val hashes = mutableListOf<String>()
            hashes.addAll(block.payload.transferTransactions.map { it.footer.hash })
            hashes.addAll(block.payload.voteTransactions.map { it.footer.hash })
            hashes.addAll(block.payload.delegateTransactions.map { it.footer.hash })
            hashes.add(block.payload.rewardTransaction[0].footer.hash)
            val transactionsMerkleHash = MainBlockPayload.calculateMerkleRoot(hashes)

            if (block.payload.merkleHash != transactionsMerkleHash) {
                return false
            }
        }

        fullSyncCursor.cursor = block.height
        return true
    }

    private fun isValidLightBlocks(block: Block, nextBlock: Block): Boolean =
        isValidBlocksHashes(block, nextBlock) && isValidBlockState(block)


    private fun isValidBlocksHashes(block: Block, nextBlock: Block): Boolean = (block.hash == nextBlock.previousHash)

    //TODO: must to be implemented
    private fun isValidBlockState(block: Block): Boolean = true

}
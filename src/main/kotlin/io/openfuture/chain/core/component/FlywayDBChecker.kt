package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import org.flywaydb.core.api.callback.Callback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.springframework.stereotype.Component

@Component
class FlywayDBChecker(
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockService: GenesisBlockService,
    private val blockService: BlockService,
    private val syncCursor: SyncCursor,private val chainSynchronizer: ChainSynchronizer
) : Callback {

    private var cursorFlag: Boolean = true


    override fun handle(event: Event?, context: Context?) {
        if(!isValidDb()){
           // rollback(syncCursor)
        }
    }

    override fun canHandleInTransaction(event: Event?, context: Context?): Boolean = true

    override fun supports(event: Event?, context: Context?): Boolean = (Event.AFTER_MIGRATE == event)

    private fun isValidDb(): Boolean {
        val epochHeight = consensusProperties.epochHeight!!
        syncCursor.fullCursor = genesisBlockService.getByEpochIndex(1L)!!
        var indexFrom = syncCursor.fullCursor.height
        var indexTo = indexFrom + epochHeight
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

                if (!isValidBlocks(block!!, nextBlock)) {
                    return false
                }

                block = nextBlock
            }
        } while (!blocks.isEmpty())
        if (!isValidBlock(block!!)) {
            return false
        }
        return true
    }

    private fun isValidBlocks(block: Block, nextBlock: Block): Boolean {
        if (!isValidBlocksHashes(block, nextBlock)) {
            return false
        }
        if (!isValidBlock(block)) {
            return false
        }
        return true
    }

    private fun isValidBlock(block: Block): Boolean {
        if (!isValidBlockState(block)) {
            return false
        }
        if (!isValidTransactions(block)) {
            cursorFlag = false
        }
        if (cursorFlag) {
            syncCursor.fullCursor = block
        }
        return true
    }

    private fun isValidTransactions(block: Block): Boolean {
        return if (block is MainBlock) {
            val hashes = mutableListOf<String>()
            hashes.addAll(block.payload.transferTransactions.map { it.footer.hash })
            hashes.addAll(block.payload.voteTransactions.map { it.footer.hash })
            hashes.addAll(block.payload.delegateTransactions.map { it.footer.hash })
            hashes.add(block.payload.rewardTransaction[0].footer.hash)
            val transactionsMerkleHash = MainBlockPayload.calculateMerkleRoot(hashes)

            block.payload.merkleHash == transactionsMerkleHash
        } else {
            true
        }
    }

    private fun isValidBlocksHashes(block: Block, nextBlock: Block): Boolean = (block.hash == nextBlock.previousHash)

    //TODO: must to be implemented
    private fun isValidBlockState(block: Block): Boolean = true

}
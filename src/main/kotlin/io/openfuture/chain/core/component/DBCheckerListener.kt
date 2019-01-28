package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import org.springframework.boot.autoconfigure.jdbc.DataSourceSchemaCreatedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class DBCheckerListener(
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockService: GenesisBlockService,
    private val blockService: BlockService,
    private val syncCursor: SyncCursor
) : ApplicationListener<DataSourceSchemaCreatedEvent> {

    override fun onApplicationEvent(event: DataSourceSchemaCreatedEvent) {
        if (!isValidDb()) {
            deleteInvalidChainPart()
        }
    }

    private fun deleteInvalidChainPart() {
        val heightFrom = syncCursor.fullCursor.height + 1
        val heightTo = blockService.getLast().height
        val heightsToDelete = ArrayList<Long>()
        for (i in heightFrom..heightTo) {
            heightsToDelete.add(i)
        }
        blockService.deleteByHeightIn(heightsToDelete)
    }

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
        return isValidBlock(block!!)
    }

    private fun isValidBlocks(block: Block, nextBlock: Block): Boolean {
        if (!isValidBlocksHashes(block, nextBlock)) {
            syncCursor.fullCursor = block
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
            return false
        }
        syncCursor.fullCursor = block
        return true
    }

    private fun isValidTransactions(block: Block): Boolean {
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
        return true
    }

    private fun isValidBlocksHashes(block: Block, nextBlock: Block): Boolean = (block.hash == nextBlock.previousHash)

    //TODO: must to be implemented
    private fun isValidBlockState(block: Block): Boolean = true

}
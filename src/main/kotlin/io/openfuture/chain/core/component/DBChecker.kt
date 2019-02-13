package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DBChecker(
    private val blockManager: BlockManager,
    private val consensusProperties: ConsensusProperties
) {

    @Transactional
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
            LIGHT -> {
                // todo("prepare db in LIGHT mode")
            }
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

}
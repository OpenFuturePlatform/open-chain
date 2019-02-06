package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import org.springframework.stereotype.Component

@Component
class DBChecker(
    private val blockService: BlockService,
    private val consensusProperties: ConsensusProperties,
    private val transactionService: TransactionService
) {

    fun prepareDB(syncMode: SyncMode): Boolean {
        val lastBlockHeight = blockService.getLast().height
        val validBlockHeight = lastValidBlockHeight(syncMode)
        if (validBlockHeight < lastBlockHeight) {
            deleteInvalidChainPart(validBlockHeight, lastBlockHeight)
            return false
        }
        return true
    }

    private fun deleteInvalidChainPart(height: Long, heightTo: Long) {
        val heightsForDelete = LongRange(height + 1, heightTo).toList()
        blockService.deleteByHeightIn(heightsForDelete)
    }

    private fun lastValidBlockHeight(syncMode: SyncMode): Long {
        val epochHeight = consensusProperties.epochHeight!! + 1L
        var indexFrom = 1L
        var indexTo = indexFrom + epochHeight
        var heights = (indexFrom..indexTo).toList()
        var blocks = blockService.getAllByHeightIn(heights).toMutableList()
        var result = blocks.first()
        val lastChainBlock = blockService.getLast()
        while (!blocks.isEmpty()) {
            result = validateEpoch(blocks, syncMode) ?: result
            if (result != blocks.last()) {
                break
            }
            indexFrom += indexTo
            indexTo += epochHeight
            heights = (indexFrom..indexTo).toList()
            blocks = blockService.getAllByHeightIn(heights).toMutableList()
        }

        return if (!isValidBlock(lastChainBlock, syncMode)) {
            result.height - 1
        } else {
            result.height
        }
    }

    private fun validateEpoch(blocks: List<Block>, syncMode: SyncMode): Block? {
        var result: Block? = null
        for (i in blocks.indices) {

            if (i == blocks.lastIndex) {
                continue
            }

            val current = blocks[i]
            if (!isValidBlock(current, syncMode)) {
                return result
            }

            val next = blocks[i + 1]
            result = current

            if (!isValidBlocksHashes(current, next)) {
                return result
            }
        }
        return result
    }

    private fun isValidBlock(block: Block, syncMode: SyncMode): Boolean {
        if (!isValidBlockState(block)) {
            return false
        }
        if (!blockService.isValidHash(block)) {
            return false
        }
        if (FULL == syncMode) {
            return isValidTransactions(block)
        }
        if (LIGHT == syncMode && block is MainBlock) {
            val transactions =
                block.payload.transferTransactions +
                    block.payload.delegateTransactions +
                    block.payload.voteTransactions
            return transactions.isEmpty()
        }
        return true
    }

    private fun isValidBlocksHashes(block: Block, nextBlock: Block): Boolean = (block.hash == nextBlock.previousHash)

    private fun isValidTransactions(block: Block): Boolean {
        if (block is MainBlock) {
            val hashes = mutableListOf<String>()
            hashes.addAll(block.payload.transferTransactions.map { transactionService.createHash(it.header, it.payload) })
            hashes.addAll(block.payload.voteTransactions.map { transactionService.createHash(it.header, it.payload) })
            hashes.addAll(block.payload.delegateTransactions.map { transactionService.createHash(it.header, it.payload) })
            val rewardTransaction = block.payload.rewardTransaction[0]
            hashes.add(transactionService.createHash(rewardTransaction.header, rewardTransaction.payload))

            if (block.payload.merkleHash != MainBlockPayload.calculateMerkleRoot(hashes)) {
                return false
            }
        }
        return true
    }

    private fun isValidBlockState(block: Block): Boolean {
        if (block is MainBlock) {
            val stateHashes = listOf(block.payload.delegateStates, block.payload.walletStates)
                .flatMap { states -> states.map { it.toMessage().getHash() } }

            if (block.payload.stateHash != MainBlockPayload.calculateMerkleRoot(stateHashes)) {
                return false
            }
        }
        return true
    }

}
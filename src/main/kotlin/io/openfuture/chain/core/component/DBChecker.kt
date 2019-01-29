package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.sync.SyncMode
import org.springframework.stereotype.Component
import java.util.*

@Component
class DBChecker(
    private val blockService: BlockService,
    private val genesisBlockService: GenesisBlockService,
    private val consensusProperties: ConsensusProperties,
    private val transactionService: TransactionService
) {

    fun prepareDB(syncMode: SyncMode): Boolean {
        val lastBlockHeight = blockService.getLast().height
        val validBlockHeight = lastValidBlockHeight(syncMode)
        if (validBlockHeight < lastBlockHeight) {
            deleteInvalidChainPart(validBlockHeight)
            return false
        }
        return true
    }

    private fun deleteInvalidChainPart(height: Long) {
        val heightFrom = height + 1
        val heightTo = blockService.getLast().height
        val heightsToDelete = ArrayList<Long>()
        for (i in heightFrom..heightTo) {
            heightsToDelete.add(i)
        }
        blockService.deleteByHeightIn(heightsToDelete)
    }

    private fun lastValidBlockHeight(syncMode: SyncMode): Long {
        val firstGenesisBlock = genesisBlockService.getByEpochIndex(1L)!!
        val epochHeight = consensusProperties.epochHeight!!
        var indexFrom = firstGenesisBlock.height
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

                if (!isValidBlock(block!!, syncMode)) {
                    return block.height - 1
                }
                if (!isValidBlocksHashes(block, nextBlock)) {
                    return block.height
                }

                block = nextBlock
            }
        } while (!blocks.isEmpty())
        if (!isValidBlock(block!!, syncMode)) {
            return block.height - 1
        }
        return block.height
    }

    private fun isValidBlock(block: Block, syncMode: SyncMode): Boolean {
        if (!isValidBlockState(block)) {
            return false
        }
        if (!blockService.isValidHash(block)) {
            return false
        }
        if (SyncMode.FULL == syncMode) {
            return isValidTransactions(block)
        }
        if (SyncMode.LIGHT == syncMode && block is MainBlock) {
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

            val transactionsMerkleHash = MainBlockPayload.calculateMerkleRoot(hashes)
            if (block.payload.merkleHash != transactionsMerkleHash) {
                return false
            }
        }
        return true
    }

    private fun isValidBlockState(block: Block): Boolean {
        if (block is MainBlock) {
            val payload = block.payload
            val delegateHashes = payload.delegateStates.map { it.toMessage().getHash() }
            val walletHashes = payload.walletStates.map { it.toMessage().getHash() }
            val stateHash = MainBlockPayload.calculateMerkleRoot(delegateHashes + walletHashes)
            if (stateHash != payload.stateHash) {
                return false
            }
        }
        return true
    }

}
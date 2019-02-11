package io.openfuture.chain.core.component

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.HashUtils.doubleSha256
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
import org.springframework.stereotype.Component

@Component
class DBChecker(
    private val blockService: BlockService,
    private val transactionManager: TransactionManager,
    private val stateManager: StateManager,
    private val receiptService: ReceiptService,
    private val consensusProperties: ConsensusProperties
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
        if (block.hash != ByteUtils.toHexString(HashUtils.doubleSha256(block.getBytes()))) {
            return false
        }
        if (FULL == syncMode) {
            return isValidTransactions(block) && isValidBlockReceipts(block)
        }
        if (LIGHT == syncMode && block is MainBlock) {
            val delegateTxCount = transactionManager.getCountDelegateTransactionsByBlock(block)
            val transferTxCount = transactionManager.getCountTransferTransactionsByBlock(block)
            val voteTxCount = transactionManager.getCountVoteTransactionsByBlock(block)
            return (delegateTxCount + transferTxCount + voteTxCount) == 0L
        }
        return true
    }

    private fun isValidBlocksHashes(block: Block, nextBlock: Block): Boolean = (block.hash == nextBlock.previousHash)

    private fun isValidTransactions(block: Block): Boolean {
        if (block is MainBlock) {
            val delegateTxHashes = transactionManager.getAllDelegateTransactionsByBlock(block)
                .map { toHexString(doubleSha256(it.getBytes())) }
            val transferTxHashes = transactionManager.getAllTransferTransactionsByBlock(block)
                .map { toHexString(doubleSha256(it.getBytes())) }
            val voteTxHashes = transactionManager.getAllVoteTransactionsByBlock(block)
                .map { toHexString(doubleSha256(it.getBytes())) }
            val rewardTxHash = toHexString(doubleSha256(transactionManager.getRewardTransactionByBlock(block).getBytes()))

            val hashes = delegateTxHashes + transferTxHashes + voteTxHashes + rewardTxHash

            if (block.getPayload().transactionMerkleHash != HashUtils.calculateMerkleRoot(hashes)) {
                return false
            }
        }
        return true
    }

    private fun isValidBlockState(block: Block): Boolean {
        if (block is MainBlock) {
            val delegateStateHashes = stateManager.getAllDelegateStatesByBlock(block)
                .map { toHexString(doubleSha256(it.getBytes())) }
            val accountStateHashes = stateManager.getAllAccountStatesByBlock(block)
                .map { toHexString(doubleSha256(it.getBytes())) }

            val hashes = delegateStateHashes + accountStateHashes

            if (block.getPayload().transactionMerkleHash != HashUtils.calculateMerkleRoot(hashes)) {
                return false
            }
        }
        return true
    }

    private fun isValidBlockReceipts(block: Block): Boolean {
        if (block is MainBlock) {
            val receiptHashes = receiptService.getAllByBlock(block)
                .map { toHexString(doubleSha256(it.getBytes())) }

            if (block.getPayload().transactionMerkleHash != HashUtils.calculateMerkleRoot(receiptHashes)) {
                return false
            }
        }
        return true
    }

}
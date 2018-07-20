package io.openfuture.chain.block.validation

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service

@Service
class BlockValidationProvider(
    private val mainBlockService: BlockService<MainBlock>,
    private val genesisBlockService: BlockService<GenesisBlock>,
    private val timeSlot: TimeSlot,
    private val clock: NodeClock
) {

    fun isValid(block: Block): Boolean {
        val currentTime = clock.networkTime()

        val lastBlock: Block?
        val blockIsValid: Boolean
        if (block is MainBlock) {
            blockIsValid = mainBlockService.isValid(block)
            lastBlock = mainBlockService.getLast()
        } else if (block is GenesisBlock) {
            blockIsValid = genesisBlockService.isValid(block)
            lastBlock = genesisBlockService.getLast()
        } else {
            throw IllegalArgumentException("wrong block type is found")
        }

        return blockIsValid
                && timeSlot.verifyTimeSlot(currentTime, block)
                && verifyHash(block)
                && verifyPreviousHash(block, lastBlock)
                && verifyHeight(block, lastBlock)
                && verifyTimestamp(block, lastBlock)
    }

    private fun verifyHash(block: Block): Boolean {
        val calculatedHashBytes = BlockUtils.calculateHash(
            block.previousHash,
            block.timestamp,
            block.height,
            block.merkleHash)
        return (ByteUtils.toHexString(calculatedHashBytes) == block.hash)
    }

    private fun verifyPreviousHash(block: Block, lastBlock: Block): Boolean = (block.previousHash == lastBlock.hash)

    private fun verifyTimestamp(block: Block, lastBlock: Block): Boolean = (block.timestamp > lastBlock.timestamp)

    private fun verifyHeight(block: Block, lastBlock: Block): Boolean = (block.height == lastBlock.height + 1)

}
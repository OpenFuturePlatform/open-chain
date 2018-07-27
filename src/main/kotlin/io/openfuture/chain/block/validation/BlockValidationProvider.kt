package io.openfuture.chain.block.validation

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.service.BlockService
import org.springframework.stereotype.Service

@Service
class BlockValidationProvider(
    private val blockService: BlockService<Block>,
    private val mainBlockService: BlockService<MainBlock>,
    private val genesisBlockService: BlockService<GenesisBlock>,
    private val timeSlot: TimeSlot,
    private val clock: NodeClock
) {

    fun isValid(block: Block): Boolean {
        val currentTime = clock.networkTime()

        val blockIsValid: Boolean = when (block) {
            is MainBlock -> mainBlockService.isValid(block)
            is GenesisBlock -> genesisBlockService.isValid(block)
            else -> throw IllegalArgumentException("Wrong block type is found")
        }

        val lastBlock = blockService.getLast()
        return blockIsValid
                && timeSlot.verifyTimeSlot(currentTime, block)
                && verifyBlockSignature(block)
                && verifyPreviousHash(block, lastBlock)
                && verifyHeight(block, lastBlock)
                && verifyTimestamp(block, lastBlock)
    }

    private fun verifyBlockSignature(block: Block): Boolean {
        if (block is MainBlock) {
            return SignatureManager.verify(
                (block.previousHash + block.merkleHash + block.timestamp + block.height).toByteArray(),
                block.signature,
                HashUtils.fromHexString(block.publicKey))
        }
        return SignatureManager.verify(
            (block.previousHash + block.timestamp + block.height).toByteArray(),
            block.signature,
            HashUtils.fromHexString(block.publicKey))
    }

    private fun verifyPreviousHash(block: Block, lastBlock: Block): Boolean = (block.previousHash == lastBlock.hash)

    private fun verifyTimestamp(block: Block, lastBlock: Block): Boolean = (block.timestamp > lastBlock.timestamp)

    private fun verifyHeight(block: Block, lastBlock: Block): Boolean = (block.height == lastBlock.height + 1)

}
package io.openfuture.chain.block.validation

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class BlockValidationProvider(
    private val applicationContext: ApplicationContext,
    private val blockService: BlockService,
    private val timeSlot: TimeSlot,
    private val clock: NodeClock
) {

    private val validators = HashMap<Int, BlockValidator>()


    @PostConstruct
    fun init() {
        val blockValidators = applicationContext.getBeansOfType(BlockValidator::class.java).values
        blockValidators.forEach {
            validators[it.getTypeId()] = it
        }
    }

    fun isValid(block: Block): Boolean {
        val currentTime = clock.networkTime()
        val blockTypeId = block.typeId
        val blockValidator = validators[blockTypeId] ?: throw IllegalArgumentException("Unknown block type")
        val lastBlock = blockService.getLast()

        return timeSlot.verifyTimeSlot(currentTime, block)
                && blockValidator.isValid(block)
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
package io.openfuture.chain.block.validation

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class BlockValidationProvider(
    private val applicationContext: ApplicationContext,
    private val blockService: BlockService,
    @Value("\${block.time.slot}") private val interval: Long
) {

    private val validators = HashMap<Int, BlockValidator>()
    private var epochTime: Long = 0L


    @PostConstruct
    fun init() {
        val blockValidators = applicationContext.getBeansOfType(BlockValidator::class.java).values
        blockValidators.forEach {
            validators[it.getTypeId()] = it
        }
    }

    fun isValid(block: Block): Boolean {
        val currentTime = System.currentTimeMillis()
        if (getSlotNumber(currentTime) != getSlotNumber(block.timestamp)) {
            return false
        }

        val blockTypeId = block.typeId

        val blockValidator = validators[blockTypeId]

        if (!blockValidator!!.isValid(block)) {
            return false
        }

        val lastBlock = blockService.getLast()
        if (!verifyHash(block)
                || !verifyPreviousHash(block, lastBlock)
                || !verifyHeight(block, lastBlock)
                || !verifyTimestamp(block, lastBlock)) {
            return false
        }

        return true
    }

    fun getSlotNumber(time: Long): Long {
        return (time - epochTime) / interval / 2
    }

    fun setEpochTime(value: Long) {
        this.epochTime = value
    }

    private fun verifyHash(block: Block): Boolean {
        val calculatedHashBytes = BlockUtils.calculateHash(
            block.previousHash,
            block.timestamp,
            block.height,
            block.merkleHash)
        return (HashUtils.bytesToHexString(calculatedHashBytes) == block.hash)
    }

    private fun verifyPreviousHash(block: Block, lastBlock: Block): Boolean {
        return (block.previousHash == lastBlock.hash)
    }

    private fun verifyTimestamp(block: Block, lastBlock: Block): Boolean {
        return (block.timestamp > lastBlock.timestamp)
    }

    private fun verifyHeight(block: Block, lastBlock: Block): Boolean {
        return (block.height == lastBlock.height + 1)
    }

}
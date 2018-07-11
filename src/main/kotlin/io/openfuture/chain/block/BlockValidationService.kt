package io.openfuture.chain.block

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.util.BlockUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class BlockValidationService(
    private val applicationContext: ApplicationContext,
    @Value("\${block.time.slot}") private val interval: Long
) {

    private val validators = HashMap<Int, BlockValidator>()

    private var epochTime: Long = 0L

    @PostConstruct
    fun init() {
        val blockValidators = applicationContext.getBeansOfType(BlockValidator::class.java).values
        blockValidators.forEach {
            validators[it.getVersion()] = it
        }
    }

    fun isValid(block: Block, lastChainBlock: Block): Boolean {
        val currentTime = System.currentTimeMillis()
        if (getSlotNumber(currentTime) != getSlotNumber(block.timestamp)) {
            return false
        }

        val blockVersion = block.version

        val blockValidator = validators[blockVersion]

        if (!blockValidator!!.isValid(block)) {
            return false
        }

        val calculatedHashBytes = BlockUtils.calculateHash(
            block.previousHash,
            block.merkleHash,
            block.timestamp,
            block.height)
        if (HashUtils.bytesToHexString(calculatedHashBytes) != block.hash) {
            return false
        }

        if (block.previousHash != lastChainBlock.hash) {
            return false
        }

        if (block.timestamp <= lastChainBlock.timestamp) {
            return false
        }

        if (block.height != lastChainBlock.height + 1) {
            return false
        }

        return true
    }

    fun getSlotNumber(time: Long): Long {
        return (time - epochTime) / interval
    }

    fun setEpochTime(value: Long) {
        this.epochTime = value
    }

}
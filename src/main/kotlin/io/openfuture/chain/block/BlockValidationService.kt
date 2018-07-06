package io.openfuture.chain.block

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class BlockValidationService(
    private val blockService: BlockService,
    private val applicationContext: ApplicationContext
) {

    private val validators = HashMap<Int, BlockValidator>()


    @PostConstruct
    fun init() {
        val blockValidators = applicationContext.getBeansOfType(BlockValidator::class.java).values
        blockValidators.forEach {
            validators[it.getVersion()] = it
        }
    }

    fun isValid(block: Block): Boolean {
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

        val lastChainBlock = blockService.getLast()
        if (lastChainBlock != null) {
            val lastBlockHeight = lastChainBlock.height
            if (block.height != lastBlockHeight + 1) {
                return false
            }

            if (block.previousHash != lastChainBlock.hash) {
                return false
            }

            if (block.timestamp <= lastChainBlock.timestamp) {
                return false
            }
        }

        return true
    }

}
package io.openfuture.chain.crypto.block

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class BlockValidator(
    private val blockService: BlockService,
    private val applicationContext: ApplicationContext
) {

    private val validators = HashMap<Int, Validator>()

    @PostConstruct
    private fun init() {
        val vs = applicationContext.getBeansOfType(Validator::class.java).values
        vs.forEach {
            validators.put(it.getVersion(), it)
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
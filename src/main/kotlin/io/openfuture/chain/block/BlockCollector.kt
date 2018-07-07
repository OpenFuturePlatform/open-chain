package io.openfuture.chain.block

import io.openfuture.chain.block.message.FullSignedBlock
import io.openfuture.chain.block.message.SignedBlock
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class BlockCollector(
    private val blockValidationService: BlockValidationService
) {

    private val lock = ReentrantReadWriteLock()

    private var signedBlocks = mutableListOf<SignedBlock>()


    fun clear() {
        try {
            lock.writeLock().lock()
            signedBlocks = mutableListOf()
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlock(block: SignedBlock) {
        try {
            lock.writeLock().lock()
            signedBlocks.add(block)
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun mergeBlockSigns(): FullSignedBlock {
        val firstBlock = signedBlocks.first()
        for (signedBlock in signedBlocks) {
            val block = signedBlock.block

            val blockIsValid = blockValidationService.isValid(block)
            if (!blockIsValid || signedBlock.block.hash != firstBlock.block.hash) {
                throw IllegalArgumentException("$signedBlocks has wrong block = $signedBlock")
            }

            val signature = signedBlock.signature
            // TODO we'll check signature by some service
        }

        val signatures = signedBlocks.map { it.signature }.toSet()
        return FullSignedBlock(firstBlock.block, signatures)
    }

}
package io.openfuture.chain.block

import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class BlockCollector(
    private val blockValidationService: BlockValidationService
) {

    private val lock = ReentrantReadWriteLock()

    private var signedBlocks = mutableListOf<CommunicationProtocol.SignedBlock>()

    // variable to collect the blocks from the same round only
    private lateinit var blockCollectionHash: String


    fun setBlockCollectionHash(blockCollectionHash: String) {
        try {
            lock.writeLock().lock()
            this.blockCollectionHash = blockCollectionHash
            signedBlocks = mutableListOf()
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlock(signedBlock: CommunicationProtocol.SignedBlock) {
        try {
            lock.writeLock().lock()

            if (signedBlock.block.hash == blockCollectionHash) {
                signedBlocks.add(signedBlock)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun mergeBlockSigns(): CommunicationProtocol.FullSignedBlock {
        try {
            lock.readLock().lock()

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

            return CommunicationProtocol.FullSignedBlock.newBuilder()
                .setBlock(firstBlock.block)
                .addAllSignatures(signatures)
                .build()
        } finally {
            lock.readLock().unlock()
        }
    }

}
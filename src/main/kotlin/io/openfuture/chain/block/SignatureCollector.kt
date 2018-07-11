package io.openfuture.chain.block

import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.nio.converter.BlockSignaturesConverter
import io.openfuture.chain.protocol.CommunicationProtocol.BlockSignatures
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class SignatureCollector(
    private val blockSignaturesConverter: BlockSignaturesConverter
) {

    private val lock = ReentrantReadWriteLock()
    private val scheduler = ThreadPoolTaskScheduler()

    // variable to collect the signatures from the same round only
    private lateinit var pendingBlock: PendingBlock

    fun isBlockEquals(block: Block): Boolean {
        return try {
            lock.readLock().lock()
            pendingBlock.block.hash == block.hash
        } finally {
            lock.readLock().unlock()
        }
    }

    fun getBlock(): Block {
        return try {
            lock.readLock().lock()
            pendingBlock.block
        } finally {
            lock.readLock().unlock()
        }
    }

    fun getBlockSignatures(): BlockSignatures {
        return try {
            lock.readLock().lock()
            blockSignaturesConverter.fromEntity(pendingBlock)
        } finally {
            lock.readLock().unlock()
        }
    }

    fun setPendingBlock(generatedBlock: PendingBlock) {
        try {
            lock.writeLock().lock()
            if (generatedBlock.block.hash != pendingBlock.block.hash) {

            }
            this.pendingBlock = generatedBlock
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlockSignatures(blockSignatures: PendingBlock): Boolean {
        try {
            lock.writeLock().lock()

            val block = blockSignatures.block
            if (block.hash != pendingBlock.block.hash) {
                return false
            }

            val signaturesToAdd = blockSignatures.signatures
            if (pendingBlock.signatures == signaturesToAdd) {
                return false
            }

            pendingBlock.signatures.addAll(signaturesToAdd)
        } finally {
            lock.writeLock().unlock()
        }
        return true
    }

}
package io.openfuture.chain.block

import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.nio.converter.BlockSignaturesConverter
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.BlockSignatures
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class SignatureCollector(
    private val blockSignaturesConverter: BlockSignaturesConverter
) {

    private val lock = ReentrantReadWriteLock()

    // variable to collect the signatures from the same round only
    private lateinit var pendingBlock: PendingBlock


    fun getBlock(): Block {
        try {
            lock.readLock().lock()
            return pendingBlock.block
        } finally {
            lock.readLock().unlock()
        }
    }

    fun getBlockSignatures(): BlockSignatures {
        try {
            lock.readLock().lock()
            return blockSignaturesConverter.fromEntity(pendingBlock)
        } finally {
            lock.readLock().unlock()
        }
    }

    fun setPendingBlock(generatedBlock: Block) {
        CommunicationProtocol.Packet.BodyCase.TIME_SYNC_REQUEST
        try {
            lock.writeLock().lock()
            this.pendingBlock = PendingBlock(generatedBlock)
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlockSignatures(blockSignatures: BlockSignatures): Boolean {
        try {
            lock.writeLock().lock()

            val blockToSign = blockSignaturesConverter.fromMessage(blockSignatures)
            val block = blockToSign.block
            if (block.hash != pendingBlock.block.hash) {
                return false
            }

            val signaturesToAdd = blockToSign.signatures
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
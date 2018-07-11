package io.openfuture.chain.block

import io.openfuture.chain.domain.block.SignaturePublicKeyPair
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

    private val signaturePublicKeyPairs: HashSet<SignaturePublicKeyPair> = HashSet()

    // variable to collect the signatures from the same round only
    private lateinit var pendingBlock: Block


    fun getBlockSignatures(): BlockSignatures {
        return blockSignaturesConverter.toBlockSignaturesProto(pendingBlock, signaturePublicKeyPairs)
    }

    fun setPendingBlock(generatedBlock: Block) {
        CommunicationProtocol.Packet.BodyCase.TIME_SYNC_REQUEST
        try {
            lock.writeLock().lock()
            this.pendingBlock = generatedBlock
            signaturePublicKeyPairs.clear()
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlockSignatures(blockSignatures: BlockSignatures): Boolean {
        try {
            lock.writeLock().lock()

            val block = blockSignaturesConverter.toBlock(blockSignatures)
            if (block.hash != pendingBlock.hash) {
                return false
            }

            val signaturesToAdd = blockSignaturesConverter.getSignaturePublicKeyPairs(blockSignatures)
            if (signaturePublicKeyPairs == signaturesToAdd) {
                return false
            }

            signaturePublicKeyPairs.addAll(signaturesToAdd)
        } finally {
            lock.writeLock().unlock()
        }
        return true
    }

}
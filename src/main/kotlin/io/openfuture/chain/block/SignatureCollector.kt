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

    private val signaturePublicKeyPairs = mutableSetOf<SignaturePublicKeyPair>()

    // variable to collect the signatures from the same round only
    private lateinit var pendingBlock: Block


    fun getBlockSignatures(): BlockSignatures {
        val blockSignaturesBuilder = BlockSignatures.newBuilder()
        val communicationProtocolBuilder = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()

        blockSignaturesConverter.setBlockProto(blockSignaturesBuilder, pendingBlock)

        val signatures = signaturePublicKeyPairs
            .map {
                communicationProtocolBuilder
                    .setSignature(it.signature)
                    .setPublicKey(it.publicKey)
                    .build()
            }.toList()

        return blockSignaturesBuilder
            .addAllSignatures(signatures)
            .build()
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

            if (!signaturePublicKeyPairs.equals(blockSignatures.signaturesList)) {
                return false
            }

            val block = blockSignaturesConverter.toBlock(blockSignatures)
            val signaturesList = blockSignatures.signaturesList.map { SignaturePublicKeyPair(it.signature, it.publicKey) }
            if (block.hash == pendingBlock.hash) {
                signaturePublicKeyPairs.addAll(signaturesList)
            }
        } finally {
            lock.writeLock().unlock()
        }
        return true
    }

}
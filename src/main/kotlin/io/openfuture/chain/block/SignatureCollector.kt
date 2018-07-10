package io.openfuture.chain.block

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.entity.Block
import io.openfuture.chain.nio.converter.BlockSignaturesConverter
import io.openfuture.chain.nio.converter.GenesisBlockConverter
import io.openfuture.chain.nio.converter.MainBlockConverter
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.BlockSignatures
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class SignatureCollector(
    private val signatureManager: SignatureManager,
    private val genesisBlockConverter: GenesisBlockConverter,
    private val blockSignaturesConverter: BlockSignaturesConverter,
    private val mainBlockConverter: MainBlockConverter
) {

    private val lock = ReentrantReadWriteLock()

    private var signaturePublicKeyPairs = HashSet<SignaturePublicKeyPair>()

    // variable to collect the blocks from the same round only
    private lateinit var pendingBlock: Block


    fun getBlockSignatures(): BlockSignatures {
        val blockSignaturesBuilder = BlockSignatures.newBuilder()
        val communicationProtocolBuiler = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()

        blockSignaturesConverter.setBlockProto(blockSignaturesBuilder, pendingBlock)

        val signatures = signaturePublicKeyPairs
            .map {
                communicationProtocolBuiler
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
            signaturePublicKeyPairs = HashSet()
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
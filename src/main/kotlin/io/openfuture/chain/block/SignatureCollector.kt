package io.openfuture.chain.block

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockVersion
import io.openfuture.chain.nio.converter.GenesisBlockConverter
import io.openfuture.chain.nio.converter.MainBlockConverter
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class SignatureCollector(
    private val signatureManager: SignatureManager,
    private val genesisBlockConverter: GenesisBlockConverter,
    private val mainBlockConverter: MainBlockConverter
) {

    private val lock = ReentrantReadWriteLock()

    private var blockSignatures = mutableListOf<CommunicationProtocol.BlockSignature>()

    // variable to collect the blocks from the same round only
    private lateinit var generatedBlock: Block


    fun setBlockTemplate(generatedBlock: Block) {
        CommunicationProtocol.Packet.BodyCase.TIME_SYNC_REQUEST
        try {
            lock.writeLock().lock()
            this.generatedBlock = generatedBlock
            blockSignatures = mutableListOf()
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlockSignature(blockSign: CommunicationProtocol.BlockSignature) {
        try {
            lock.writeLock().lock()
            if (blockSign.blockHash == generatedBlock.hash) {
                blockSignatures.add(blockSign)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun mergeBlockSignatures(): CommunicationProtocol.FullSignedBlock {
        try {
            lock.readLock().lock()

            val firstSign = blockSignatures.first()
            for (blockSign in blockSignatures) {
                if (blockSign.blockHash != generatedBlock.hash || blockSign.blockHash != firstSign.blockHash) {
                    throw IllegalArgumentException("$blockSignatures has wrong sign = $blockSign")
                }

                val hash = HashUtils.hexStringToBytes(blockSign.blockHash)
                val publicKey = HashUtils.hexStringToBytes(blockSign.publicKey)
                if (!signatureManager.verify(hash, blockSign.signature, publicKey)) {
                    throw IllegalArgumentException("$blockSign has wrong sign")
                }
            }

            val signatures = blockSignatures.map { it.signature }.toSet()

            return setBlockProto(CommunicationProtocol.FullSignedBlock.newBuilder(), generatedBlock)
                .addAllSignatures(signatures)
                .build()
        } finally {
            lock.readLock().unlock()
        }
    }

    private fun setBlockProto(
            builder: CommunicationProtocol.FullSignedBlock.Builder,
            block: Block): CommunicationProtocol.FullSignedBlock.Builder {
        if (block.version == BlockVersion.MAIN.version) {
            builder.mainBlock = mainBlockConverter.toMainBlockProto(block)
        } else if (block.version == BlockVersion.GENESIS.version) {
            builder.genesisBlock = genesisBlockConverter.toGenesisBlockProto(block)
        }
        return builder
    }

}
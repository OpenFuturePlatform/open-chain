package io.openfuture.chain.nio.converter

import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.BlockVersion
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.BlockSignatures
import org.springframework.stereotype.Component

@Component
class BlockSignaturesConverter(
    private val genesisBlockConverter: GenesisBlockConverter,
    private val mainBlockConverter: MainBlockConverter,
    private val signaturePublicKeyPairConverter: SignaturePublicKeyPairConverter
): MessageConverter<PendingBlock, BlockSignatures> {

    private val blockSignaturesBuilder = BlockSignatures.newBuilder()

    override fun fromEntity(entity: PendingBlock): BlockSignatures {
        val block = entity.block

        if (block.version == BlockVersion.MAIN.version) {
            blockSignaturesBuilder.mainBlock = mainBlockConverter.fromEntity(block)
        } else if (block.version == BlockVersion.GENESIS.version) {
            blockSignaturesBuilder.genesisBlock = genesisBlockConverter.fromEntity(block)
        }

        val signatures = entity.signatures
            .map { signaturePublicKeyPairConverter.fromEntity(it) }.toList()

        return blockSignaturesBuilder
            .addAllSignatures(signatures)
            .build()
    }

    override fun fromMessage(message: BlockSignatures): PendingBlock {
        val block = when {
            message.mainBlock != null -> mainBlockConverter.fromMessage(message.mainBlock)
            message.genesisBlock != null -> genesisBlockConverter.fromMessage(message.genesisBlock)
            else -> throw IllegalArgumentException("$message has no block")
        }

        val signatures = message.signaturesList.map { SignaturePublicKeyPair(it.signature, it.publicKey) }.toHashSet()
        return PendingBlock(block, signatures)
    }

}
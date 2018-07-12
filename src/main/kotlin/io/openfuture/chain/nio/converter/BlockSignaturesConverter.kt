package io.openfuture.chain.nio.converter

import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.entity.BlockType
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

        if (block.typeId == BlockType.MAIN.id) {
            blockSignaturesBuilder.mainBlock = mainBlockConverter.fromEntity(block)
        } else if (block.typeId == BlockType.GENESIS.id) {
            blockSignaturesBuilder.genesisBlock = genesisBlockConverter.fromEntity(block)
        }

        val signature = signaturePublicKeyPairConverter.fromEntity(entity.signature)
        return blockSignaturesBuilder.setSignature(signature).build()
    }

    override fun fromMessage(message: BlockSignatures): PendingBlock {
        val block = when {
            message.mainBlock.typeId == BlockType.MAIN.id -> mainBlockConverter.fromMessage(message.mainBlock)
            message.genesisBlock.typeId == BlockType.GENESIS.id -> genesisBlockConverter.fromMessage(message.genesisBlock)
            else -> throw IllegalArgumentException("$message has no block")
        }

        val signature = signaturePublicKeyPairConverter.fromMessage(message.signature)
        return PendingBlock(block, signature)
    }

}
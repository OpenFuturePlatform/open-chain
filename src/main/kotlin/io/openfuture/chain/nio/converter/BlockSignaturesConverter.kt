package io.openfuture.chain.nio.converter

import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockVersion
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.protocol.CommunicationProtocol.BlockSignatures
import org.springframework.stereotype.Component

@Component
class BlockSignaturesConverter(
    private val genesisBlockConverter: GenesisBlockConverter,
    private val mainBlockConverter: MainBlockConverter
): MessageConverter<Block, BlockSignatures> {

    override fun fromEntity(entity: Block): BlockSignatures {
        val blockSignaturesBuilder = BlockSignatures.newBuilder()
        setBlockMessage(blockSignaturesBuilder, entity)
        return blockSignaturesBuilder.build()
    }

    override fun fromMessage(message: BlockSignatures): Block {
        if (message.mainBlock != null) {
            return mainBlockConverter.fromMessage(message.mainBlock)
        } else if (message.genesisBlock != null) {
            return genesisBlockConverter.fromMessage(message.genesisBlock)
        }

        throw IllegalArgumentException("$message has no block")
    }

    fun getSignaturePublicKeyPairs(blockSignatures: BlockSignatures)
            : HashSet<SignaturePublicKeyPair> {
        return blockSignatures.signaturesList.map { SignaturePublicKeyPair(it.signature, it.publicKey) }.toHashSet()
    }

    fun toBlockSignaturesProto(block: Block, signaturePublicKeyPairs: HashSet<SignaturePublicKeyPair>)
            : BlockSignatures {
        val blockSignaturesBuilder = BlockSignatures.newBuilder()
        val communicationProtocolBuilder = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()

        setBlockMessage(blockSignaturesBuilder, block)
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

    fun setBlockMessage(blockSignaturesBuilder: BlockSignatures.Builder, block: Block): BlockSignatures.Builder {
        if (block.version == BlockVersion.MAIN.version) {
            blockSignaturesBuilder.mainBlock = mainBlockConverter.fromEntity(block)
        } else if (block.version == BlockVersion.GENESIS.version) {
            blockSignaturesBuilder.genesisBlock = genesisBlockConverter.fromEntity(block)
        }
        return blockSignaturesBuilder
    }

}
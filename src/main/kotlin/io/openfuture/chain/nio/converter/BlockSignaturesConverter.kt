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
) {

    fun toBlock(blockSignatures: BlockSignatures): Block {
        if (blockSignatures.mainBlock != null) {
            return mainBlockConverter.toMainBlock(blockSignatures.mainBlock)
        } else if (blockSignatures.genesisBlock != null) {
            return genesisBlockConverter.toGenesisBlock(blockSignatures.genesisBlock)
        }

        throw IllegalArgumentException("$blockSignatures has no block")
    }

    fun getSignaturePublicKeyPairs(blockSignatures: BlockSignatures)
            : HashSet<SignaturePublicKeyPair> {
        return blockSignatures.signaturesList.map { SignaturePublicKeyPair(it.signature, it.publicKey) }.toHashSet()
    }

    fun toBlockSignaturesProto(block: Block, signaturePublicKeyPairs: HashSet<SignaturePublicKeyPair>)
            : BlockSignatures {
        val blockSignaturesBuilder = BlockSignatures.newBuilder()
        val communicationProtocolBuilder = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()

        setBlockProto(blockSignaturesBuilder, block)
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

    fun setBlockProto(blockSignaturesBuilder: BlockSignatures.Builder, block: Block): BlockSignatures.Builder {
        if (block.version == BlockVersion.MAIN.version) {
            blockSignaturesBuilder.mainBlock = mainBlockConverter.toMainBlockProto(block)
        } else if (block.version == BlockVersion.GENESIS.version) {
            blockSignaturesBuilder.genesisBlock = genesisBlockConverter.toGenesisBlockProto(block)
        }
        return blockSignaturesBuilder
    }

}
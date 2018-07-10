package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class BlockSignaturesConverter(
    private val genesisBlockConverter: GenesisBlockConverter,
    private val mainBlockConverter: MainBlockConverter
) {

    fun toBlock(blockSignatures: CommunicationProtocol.BlockSignatures): Block {
        if (blockSignatures.mainBlock != null) {
            return mainBlockConverter.toMainBlock(blockSignatures.mainBlock)
        } else if (blockSignatures.genesisBlock != null) {
            return genesisBlockConverter.toGenesisBlock(blockSignatures.genesisBlock)
        }

        throw IllegalArgumentException("$blockSignatures has no block")
    }

}
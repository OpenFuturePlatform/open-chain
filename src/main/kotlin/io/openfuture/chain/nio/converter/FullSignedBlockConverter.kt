package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class FullSignedBlockConverter(
    private val genesisBlockConverter: GenesisBlockConverter,
    private val mainBlockConverter: MainBlockConverter
) {

    fun toBlock(fullSignedBlock: CommunicationProtocol.FullSignedBlock): Block {
        if (fullSignedBlock.mainBlock != null) {
            return mainBlockConverter.toMainBlock(fullSignedBlock.mainBlock)
        } else if (fullSignedBlock.genesisBlock != null) {
            return genesisBlockConverter.toGenesisBlock(fullSignedBlock.genesisBlock)
        }

        throw IllegalArgumentException("$fullSignedBlock has no block")
    }

}
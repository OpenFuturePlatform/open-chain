package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class GenesisBlockConverter: MessageConverter<Block,  CommunicationProtocol.GenesisBlock> {

    private val genesisBlockBuilder = CommunicationProtocol.GenesisBlock.newBuilder()


    override fun fromMessage(message: CommunicationProtocol.GenesisBlock): GenesisBlock {
        return GenesisBlock(
            message.hash,
            message.height,
            message.previousHash,
            message.merkleHash,
            message.timestamp,
            message.signature,
            message.epochIndex,
            message.activeDelegateKeysList.toSet())
    }

    override fun fromEntity(entity: Block): CommunicationProtocol.GenesisBlock {
        val genesisBlock = entity as GenesisBlock
        return genesisBlockBuilder
            .setHash(genesisBlock.hash)
            .setHeight(genesisBlock.height)
            .setPreviousHash(genesisBlock.previousHash)
            .setMerkleHash(genesisBlock.merkleHash)
            .setTimestamp(genesisBlock.timestamp)
            .setEpochIndex(genesisBlock.epochIndex)
            .setSignature(genesisBlock.signature)
            .addAllActiveDelegateKeys(genesisBlock.activeDelegateKeys.toList())
            .build()
    }

}
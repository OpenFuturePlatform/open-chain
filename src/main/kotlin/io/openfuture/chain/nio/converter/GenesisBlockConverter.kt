package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class GenesisBlockConverter: MessageConverter<Block,  CommunicationProtocol.GenesisBlock> {

    override fun fromMessage(message: CommunicationProtocol.GenesisBlock): GenesisBlock {
        return GenesisBlock(
            message.hash,
            message.height,
            message.previousHash,
            message.merkleHash,
            message.timestamp,
            message.epochIndex,
            message.activeDelegateKeysList.toSet())
    }

    override fun fromEntity(entity: Block): CommunicationProtocol.GenesisBlock {
        val genesisBlock = entity as GenesisBlock
        return CommunicationProtocol.GenesisBlock.newBuilder()
            .setHash(genesisBlock.hash)
            .setHeight(genesisBlock.height)
            .setPreviousHash(genesisBlock.previousHash)
            .setMerkleHash(genesisBlock.merkleHash)
            .setTimestamp(genesisBlock.timestamp)
            .setEpochIndex(genesisBlock.epochIndex)
            .addAllActiveDelegateKeys(genesisBlock.activeDelegateKeys.toList())
            .build()
    }

}
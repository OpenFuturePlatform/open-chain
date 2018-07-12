package io.openfuture.chain.nio.converter

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class GenesisBlockConverterTests : ServiceTests() {

    private lateinit var genesisBlockConverter: GenesisBlockConverter


    companion object {
        fun createGenesisBlockMessage(
            hash: String,
            height: Long,
            previousHash: String,
            merkleHash: String,
            timestamp: Long,
            epochIndex: Long
        ): CommunicationProtocol.GenesisBlock {
            return CommunicationProtocol.GenesisBlock.newBuilder()
                .setHash(hash)
                .setHeight(height)
                .setPreviousHash(previousHash)
                .setMerkleHash(merkleHash)
                .setTimestamp(timestamp)
                .setEpochIndex(epochIndex)
                .setTypeId(BlockType.GENESIS.id)
                .addAllActiveDelegateKeys(listOf())
                .build()
        }
    }

    @Before
    fun setUp() {
        genesisBlockConverter = GenesisBlockConverter()
    }

    @Test
    fun fromMessageShouldCreateGenesisBlockEntity() {
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val epochIndex = 1L
        val genesisBlockMessage = createGenesisBlockMessage(
            hash, height, previousHash, merkleHash, timestamp, epochIndex)

        val genesisBlock = genesisBlockConverter.fromMessage(genesisBlockMessage)

        Assertions.assertThat(genesisBlock).isNotNull
        Assertions.assertThat(genesisBlock.hash).isEqualTo(hash)
        Assertions.assertThat(genesisBlock.height).isEqualTo(height)
        Assertions.assertThat(genesisBlock.merkleHash).isEqualTo(merkleHash)
        Assertions.assertThat(genesisBlock.epochIndex).isEqualTo(epochIndex)
        Assertions.assertThat(genesisBlock.previousHash).isEqualTo(previousHash)
        Assertions.assertThat(genesisBlock.timestamp).isEqualTo(timestamp)
        Assertions.assertThat(genesisBlock.activeDelegateKeys).isEmpty()
    }

    @Test
    fun fromEntityShouldCreateGenesisBlockMessage() {
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val epochIndex = 1L
        val genesisBlock = GenesisBlock(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            epochIndex,
            setOf()
        )

        val genesisBlockMessage = genesisBlockConverter.fromEntity(genesisBlock)

        Assertions.assertThat(genesisBlockMessage).isNotNull
        Assertions.assertThat(genesisBlockMessage.hash).isEqualTo(hash)
        Assertions.assertThat(genesisBlockMessage.height).isEqualTo(height)
        Assertions.assertThat(genesisBlockMessage.merkleHash).isEqualTo(merkleHash)
        Assertions.assertThat(genesisBlockMessage.epochIndex).isEqualTo(epochIndex)
        Assertions.assertThat(genesisBlockMessage.previousHash).isEqualTo(previousHash)
        Assertions.assertThat(genesisBlockMessage.timestamp).isEqualTo(timestamp)
        Assertions.assertThat(genesisBlockMessage.activeDelegateKeysList).isEmpty()
    }

}
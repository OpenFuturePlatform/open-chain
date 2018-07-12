package io.openfuture.chain.nio.converter

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions.assertThat
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
            epochIndex: Long,
            signature: String
        ): CommunicationProtocol.GenesisBlock {
            return CommunicationProtocol.GenesisBlock.newBuilder()
                .setHash(hash)
                .setHeight(height)
                .setPreviousHash(previousHash)
                .setMerkleHash(merkleHash)
                .setTimestamp(timestamp)
                .setEpochIndex(epochIndex)
                .setTypeId(BlockType.GENESIS.typeId)
                .setSignature(signature)
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
        val signature = "signature"
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val epochIndex = 1L
        val genesisBlockMessage = createGenesisBlockMessage(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            epochIndex,
            signature
        )

        val genesisBlock = genesisBlockConverter.fromMessage(genesisBlockMessage)

        assertThat(genesisBlock).isNotNull
        assertThat(genesisBlock.hash).isEqualTo(hash)
        assertThat(genesisBlock.height).isEqualTo(height)
        assertThat(genesisBlock.merkleHash).isEqualTo(merkleHash)
        assertThat(genesisBlock.epochIndex).isEqualTo(epochIndex)
        assertThat(genesisBlock.previousHash).isEqualTo(previousHash)
        assertThat(genesisBlock.timestamp).isEqualTo(timestamp)
        assertThat(genesisBlock.signature).isEqualTo(signature)
        assertThat(genesisBlock.activeDelegateKeys).isEmpty()
    }

    @Test
    fun fromEntityShouldCreateGenesisBlockMessage() {
        val signature = "signature"
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
            signature,
            epochIndex,
            setOf()
        )

        val genesisBlockMessage = genesisBlockConverter.fromEntity(genesisBlock)

        assertThat(genesisBlockMessage).isNotNull
        assertThat(genesisBlockMessage.hash).isEqualTo(hash)
        assertThat(genesisBlockMessage.height).isEqualTo(height)
        assertThat(genesisBlockMessage.merkleHash).isEqualTo(merkleHash)
        assertThat(genesisBlockMessage.epochIndex).isEqualTo(epochIndex)
        assertThat(genesisBlockMessage.previousHash).isEqualTo(previousHash)
        assertThat(genesisBlockMessage.timestamp).isEqualTo(timestamp)
        assertThat(genesisBlockMessage.signature).isEqualTo(signature)
        assertThat(genesisBlockMessage.activeDelegateKeysList).isEmpty()
    }

}
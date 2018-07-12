package io.openfuture.chain.nio.converter

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class MainBlockConverterTests : ServiceTests() {

    @Mock private lateinit var transactionConverter: TransactionConverter

    private lateinit var mainBlockConverter: MainBlockConverter


    companion object {
        fun createMainBlock(
            hash: String,
            height: Long,
            previousHash: String,
            merkleHash: String,
            timestamp: Long,
            signature: String
        ): CommunicationProtocol.MainBlock {
            return CommunicationProtocol.MainBlock.newBuilder()
                .setHash(hash)
                .setHeight(height)
                .setPreviousHash(previousHash)
                .setMerkleHash(merkleHash)
                .setTimestamp(timestamp)
                .setSignature(signature)
                .setTypeId(BlockType.MAIN.typeId)
                .addAllTransactions(listOf())
                .build()
        }
    }

    @Before
    fun setUp() {
        mainBlockConverter = MainBlockConverter(transactionConverter)
    }

    @Test
    fun fromMessageShouldCreateMainBlockEntity() {
        val signature = "signature"
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val mainBlockMessage = createMainBlock(hash, height, previousHash, merkleHash, timestamp, signature)

        val mainBlock = mainBlockConverter.fromMessage(mainBlockMessage)

        Assertions.assertThat(mainBlock).isNotNull
        Assertions.assertThat(mainBlock.hash).isEqualTo(hash)
        Assertions.assertThat(mainBlock.height).isEqualTo(height)
        Assertions.assertThat(mainBlock.merkleHash).isEqualTo(merkleHash)
        Assertions.assertThat(mainBlock.signature).isEqualTo(signature)
        Assertions.assertThat(mainBlock.previousHash).isEqualTo(previousHash)
        Assertions.assertThat(mainBlock.timestamp).isEqualTo(timestamp)
        Assertions.assertThat(mainBlock.transactions).isEmpty()
    }

    @Test
    fun fromEntityShouldCreateMainBlockEntity() {
        val signature = "signature"
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val mainBlock = MainBlock(hash, height, previousHash, merkleHash, timestamp, signature, listOf())
        val mainBlockMessage = mainBlockConverter.fromEntity(mainBlock)

        Assertions.assertThat(mainBlockMessage).isNotNull
        Assertions.assertThat(mainBlockMessage.hash).isEqualTo(hash)
        Assertions.assertThat(mainBlockMessage.height).isEqualTo(height)
        Assertions.assertThat(mainBlockMessage.merkleHash).isEqualTo(merkleHash)
        Assertions.assertThat(mainBlockMessage.signature).isEqualTo(signature)
        Assertions.assertThat(mainBlockMessage.previousHash).isEqualTo(previousHash)
        Assertions.assertThat(mainBlockMessage.timestamp).isEqualTo(timestamp)
        Assertions.assertThat(mainBlockMessage.transactionsList).isEmpty()
    }

}
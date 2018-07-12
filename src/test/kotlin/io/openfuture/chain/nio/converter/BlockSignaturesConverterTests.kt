package io.openfuture.chain.nio.converter

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class BlockSignaturesConverterTests : ServiceTests() {

    @Mock private lateinit var genesisBlockConverter: GenesisBlockConverter
    @Mock private lateinit var mainBlockConverter: MainBlockConverter
    @Mock private lateinit var signaturePublicKeyPairConverter: SignaturePublicKeyPairConverter

    private lateinit var blockSignaturesConverter: BlockSignaturesConverter


    @Before
    fun setUp() {
        blockSignaturesConverter = BlockSignaturesConverter(
            genesisBlockConverter,
            mainBlockConverter,
            signaturePublicKeyPairConverter
        )
    }

    @Test
    fun fromEntityShouldCreateMainBlockSignaturesMessageByPendingBlock() {
        val signature = "signature"
        val publicKey = "publicKey"
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val block = MainBlock(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            signature,
            listOf()
        )
        val mainBlock = createMainBlock(hash, height, previousHash, merkleHash, timestamp, signature)
        val signatureMessage = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
            .setSignature(signature)
            .setPublicKey(publicKey)
            .build()
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val pendingBlock = PendingBlock(block, signaturePublicKeyPair)

        given(mainBlockConverter.fromEntity(block)).willReturn(mainBlock)
        given(signaturePublicKeyPairConverter.fromEntity(signaturePublicKeyPair)).willReturn(signatureMessage)

        val blockSignatures = blockSignaturesConverter.fromEntity(pendingBlock)

        Assertions.assertThat(blockSignatures.genesisBlock.typeId).isEqualTo(0)
        Assertions.assertThat(blockSignatures.mainBlock).isNotNull
        Assertions.assertThat(blockSignatures.mainBlock.hash).isEqualTo(hash)
        Assertions.assertThat(blockSignatures.mainBlock.height).isEqualTo(height)
        Assertions.assertThat(blockSignatures.mainBlock.merkleHash).isEqualTo(merkleHash)
        Assertions.assertThat(blockSignatures.mainBlock.signature).isEqualTo(signature)
        Assertions.assertThat(blockSignatures.mainBlock.previousHash).isEqualTo(previousHash)
        Assertions.assertThat(blockSignatures.mainBlock.timestamp).isEqualTo(timestamp)
        Assertions.assertThat(blockSignatures.mainBlock.transactionsList).isEmpty()
        Assertions.assertThat(blockSignatures.signature).isEqualTo(signatureMessage)
        Assertions.assertThat(blockSignatures.signature.signature).isEqualTo(signatureMessage.signature)
        Assertions.assertThat(blockSignatures.signature.publicKey).isEqualTo(signatureMessage.publicKey)
    }

    @Test
    fun fromEntityShouldCreateGenesisBlockSignaturesMessageByPendingBlock() {
        val signature = "signature"
        val publicKey = "publicKey"
        val hash = "hash"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val epochIndex = 1L
        val block = GenesisBlock(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            epochIndex,
            setOf()
        )
        val genesisBlock = createGenesisBlock(hash, height, previousHash, merkleHash, timestamp, epochIndex)
        val signatureMessage = createSignaturePublicKeyPair(signature, publicKey)
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val pendingBlock = PendingBlock(block, signaturePublicKeyPair)

        given(genesisBlockConverter.fromEntity(block)).willReturn(genesisBlock)
        given(signaturePublicKeyPairConverter.fromEntity(signaturePublicKeyPair)).willReturn(signatureMessage)

        val blockSignatures = blockSignaturesConverter.fromEntity(pendingBlock)

        Assertions.assertThat(blockSignatures.mainBlock.typeId).isEqualTo(0)
        Assertions.assertThat(blockSignatures.genesisBlock).isNotNull
        Assertions.assertThat(blockSignatures.genesisBlock.hash).isEqualTo(hash)
        Assertions.assertThat(blockSignatures.genesisBlock.height).isEqualTo(height)
        Assertions.assertThat(blockSignatures.genesisBlock.merkleHash).isEqualTo(merkleHash)
        Assertions.assertThat(blockSignatures.genesisBlock.epochIndex).isEqualTo(epochIndex)
        Assertions.assertThat(blockSignatures.genesisBlock.previousHash).isEqualTo(previousHash)
        Assertions.assertThat(blockSignatures.genesisBlock.timestamp).isEqualTo(timestamp)
        Assertions.assertThat(blockSignatures.genesisBlock.activeDelegateKeysList).isEmpty()
        Assertions.assertThat(blockSignatures.signature).isEqualTo(signatureMessage)
        Assertions.assertThat(blockSignatures.signature.signature).isEqualTo(signatureMessage.signature)
        Assertions.assertThat(blockSignatures.signature.publicKey).isEqualTo(signatureMessage.publicKey)
    }

    private fun createMainBlock(
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

    private fun createGenesisBlock(
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
            .setTypeId(BlockType.GENESIS.typeId)
            .addAllActiveDelegateKeys(listOf())
            .build()
    }

    private fun createSignaturePublicKeyPair(
        signature: String,
        publicKey: String
    ): CommunicationProtocol.SignaturePublicKeyPair {
        return CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
            .setSignature(signature)
            .setPublicKey(publicKey)
            .build()
    }

}
package io.openfuture.chain.nio.converter

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions.assertThat
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
            height,
            previousHash,
            merkleHash,
            timestamp,
            signature,
            listOf()
        )
        val mainBlock = MainBlockConverterTests.createMainBlock(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            signature
        )
        val signatureMessage = CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
            .setSignature(signature)
            .setPublicKey(publicKey)
            .build()
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val pendingBlock = PendingBlock(block, signaturePublicKeyPair)

        given(mainBlockConverter.fromEntity(block)).willReturn(mainBlock)
        given(signaturePublicKeyPairConverter.fromEntity(signaturePublicKeyPair)).willReturn(signatureMessage)

        val blockSignatures = blockSignaturesConverter.fromEntity(pendingBlock)

        assertThat(blockSignatures.genesisBlock.typeId).isEqualTo(0)
        assertThat(blockSignatures.mainBlock).isNotNull
        assertThat(blockSignatures.mainBlock.hash).isEqualTo(hash)
        assertThat(blockSignatures.mainBlock.height).isEqualTo(height)
        assertThat(blockSignatures.mainBlock.merkleHash).isEqualTo(merkleHash)
        assertThat(blockSignatures.mainBlock.signature).isEqualTo(signature)
        assertThat(blockSignatures.mainBlock.previousHash).isEqualTo(previousHash)
        assertThat(blockSignatures.mainBlock.timestamp).isEqualTo(timestamp)
        assertThat(blockSignatures.mainBlock.transactionsList).isEmpty()
        assertThat(blockSignatures.signature).isEqualTo(signatureMessage)
        assertThat(blockSignatures.signature.signature).isEqualTo(signatureMessage.signature)
        assertThat(blockSignatures.signature.publicKey).isEqualTo(signatureMessage.publicKey)
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
            height,
            previousHash,
            timestamp,
            signature,
            epochIndex,
            setOf()
        )
        val genesisBlock = GenesisBlockConverterTests.createGenesisBlockMessage(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            epochIndex,
            signature
        )
        val signatureMessage = SignaturePublicKeyPairConverterTests.createSignaturePublicKeyPair(signature, publicKey)
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val pendingBlock = PendingBlock(block, signaturePublicKeyPair)

        given(genesisBlockConverter.fromEntity(block)).willReturn(genesisBlock)
        given(signaturePublicKeyPairConverter.fromEntity(signaturePublicKeyPair)).willReturn(signatureMessage)

        val blockSignatures = blockSignaturesConverter.fromEntity(pendingBlock)

        assertThat(blockSignatures.mainBlock.typeId).isEqualTo(0)
        assertThat(blockSignatures.genesisBlock).isNotNull
        assertThat(blockSignatures.genesisBlock.hash).isEqualTo(hash)
        assertThat(blockSignatures.genesisBlock.height).isEqualTo(height)
        assertThat(blockSignatures.genesisBlock.merkleHash).isEqualTo(merkleHash)
        assertThat(blockSignatures.genesisBlock.epochIndex).isEqualTo(epochIndex)
        assertThat(blockSignatures.genesisBlock.previousHash).isEqualTo(previousHash)
        assertThat(blockSignatures.genesisBlock.timestamp).isEqualTo(timestamp)
        assertThat(blockSignatures.genesisBlock.activeDelegateKeysList).isEmpty()
        assertThat(blockSignatures.signature).isEqualTo(signatureMessage)
        assertThat(blockSignatures.signature.signature).isEqualTo(signatureMessage.signature)
        assertThat(blockSignatures.signature.publicKey).isEqualTo(signatureMessage.publicKey)
    }

    @Test
    fun fromMessageShouldReturnPendingBlockWithMainBlockInside() {
        val signature = "signature"
        val publicKey = "publicKey"
        val hash = "50c7c9f117c06ac6850f73e87bbcfa20118bb9349040b82b0635081ccab98975"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = "merkleHash"
        val timestamp = 2L
        val mainBlockMessage = MainBlockConverterTests.createMainBlock(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            signature
        )
        val signatureKeyPair = SignaturePublicKeyPairConverterTests.createSignaturePublicKeyPair(signature, publicKey)
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val blockSignatures = CommunicationProtocol.BlockSignatures.newBuilder()
            .setMainBlock(mainBlockMessage)
            .setSignature(signatureKeyPair)
            .build()
        val mainBlock = MainBlock(
            height,
            previousHash,
            merkleHash,
            timestamp,
            signature,
            listOf()
        )

        given(mainBlockConverter.fromMessage(mainBlockMessage)).willReturn(mainBlock)
        given(signaturePublicKeyPairConverter.fromMessage(signatureKeyPair)).willReturn(signaturePublicKeyPair)

        val pendingBlock = blockSignaturesConverter.fromMessage(blockSignatures)

        val mainBlockResult = pendingBlock.block as MainBlock
        assertThat(mainBlockResult).isNotNull
        assertThat(mainBlockResult.hash).isEqualTo(hash)
        assertThat(mainBlockResult.height).isEqualTo(height)
        assertThat(mainBlockResult.merkleHash).isEqualTo(merkleHash)
        assertThat(mainBlockResult.signature).isEqualTo(signature)
        assertThat(mainBlockResult.previousHash).isEqualTo(previousHash)
        assertThat(mainBlockResult.timestamp).isEqualTo(timestamp)
        assertThat(mainBlockResult.transactions).isEmpty()

        assertThat(pendingBlock.signature).isEqualTo(signaturePublicKeyPair)
    }

    @Test
    fun fromMessageShouldReturnPendingBlockWithGenesisBlockInside() {
        val signature = "signature"
        val publicKey = "publicKey"
        val hash = "2c9acf0a1f90c65e343cac0c0894be7d44f4230b484e50862d2a2437af943efc"
        val height = 1L
        val previousHash = "previousHash"
        val merkleHash = ""
        val timestamp = 2L
        val epochIndex = 1L
        val genesisBlockMessage = GenesisBlockConverterTests.createGenesisBlockMessage(
            hash,
            height,
            previousHash,
            merkleHash,
            timestamp,
            epochIndex,
            signature
        )
        val signatureKeyPair = SignaturePublicKeyPairConverterTests.createSignaturePublicKeyPair(signature, publicKey)
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val blockSignatures = CommunicationProtocol.BlockSignatures.newBuilder()
            .setGenesisBlock(genesisBlockMessage)
            .setSignature(signatureKeyPair)
            .build()
        val genesisBlock = GenesisBlock(
            height,
            previousHash,
            timestamp,
            signature,
            epochIndex,
            setOf()
        )

        given(genesisBlockConverter.fromMessage(genesisBlockMessage)).willReturn(genesisBlock)
        given(signaturePublicKeyPairConverter.fromMessage(signatureKeyPair)).willReturn(signaturePublicKeyPair)

        val pendingBlock = blockSignaturesConverter.fromMessage(blockSignatures)

        val genesisBlockResult = pendingBlock.block as GenesisBlock
        assertThat(genesisBlockResult).isNotNull
        assertThat(genesisBlockResult.hash).isEqualTo(hash)
        assertThat(genesisBlockResult.height).isEqualTo(height)
        assertThat(genesisBlockResult.merkleHash).isEqualTo(merkleHash)
        assertThat(genesisBlockResult.epochIndex).isEqualTo(epochIndex)
        assertThat(genesisBlockResult.previousHash).isEqualTo(previousHash)
        assertThat(genesisBlockResult.timestamp).isEqualTo(timestamp)
        assertThat(genesisBlockResult.activeDelegateKeys).isEmpty()

        assertThat(pendingBlock.signature).isEqualTo(signaturePublicKeyPair)
    }

}
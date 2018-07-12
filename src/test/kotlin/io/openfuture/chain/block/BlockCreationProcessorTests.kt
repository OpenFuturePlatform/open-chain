package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.BlockCreationEvent
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class BlockCreationProcessorTests: ServiceTests() {

    @Mock private lateinit var blockService: BlockService
    @Mock private lateinit var signatureCollector: SignatureCollector
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var signatureManager: SignatureManager
    @Mock private lateinit var blockValidationService: BlockValidationProvider
    @Mock private lateinit var consensusService: ConsensusService

    private lateinit var processor: BlockCreationProcessor

    @Before
    fun init() {
        val block = createMainBlock()
        given(blockService.getLastMain()).willReturn(block)
        processor = BlockCreationProcessor(
            blockService, signatureCollector,
            keyHolder, signatureManager,
            blockValidationService, consensusService)
    }

    @Test
    fun approveBlockShouldValidateAndSignInboundBlock() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)
        val hashAsBytes = HashUtils.hexStringToBytes(block.hash)
        val keyAsBytes = HashUtils.hexStringToBytes(pendingBlock.signature.publicKey)

        given(blockValidationService.isValid(block)).willReturn(true)
        given(signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)).willReturn(true)
        given(signatureCollector.addBlockSignature(pendingBlock)).willReturn(true)
        given(keyHolder.getPrivateKey()).willReturn("prvKey".toByteArray())
        given(signatureManager.sign(hashAsBytes, keyHolder.getPrivateKey())).willReturn("sign")
        given(keyHolder.getPublicKey()).willReturn("pubKey".toByteArray())

        val resultBlock = processor.approveBlock(pendingBlock)

        assertThat(resultBlock.block).isEqualTo(pendingBlock.block)
        assertThat(resultBlock.block).isNotEqualTo(pendingBlock.signature)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockIsInvalid() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)
        given(blockValidationService.isValid(block)).willReturn(false)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfSignatureVerificationFailed() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)
        val hashAsBytes = HashUtils.hexStringToBytes(block.hash)
        val keyAsBytes = HashUtils.hexStringToBytes(pendingBlock.signature.publicKey)

        given(blockValidationService.isValid(block)).willReturn(true)
        given(signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)).willReturn(true)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockSignatureIsAlreadyExists() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)
        val hashAsBytes = HashUtils.hexStringToBytes(block.hash)
        val keyAsBytes = HashUtils.hexStringToBytes(pendingBlock.signature.publicKey)

        given(blockValidationService.isValid(block)).willReturn(true)
        given(signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)).willReturn(true)
        given(signatureCollector.addBlockSignature(pendingBlock)).willReturn(false)

        processor.approveBlock(pendingBlock)
    }

    @Test
    fun fireBlockCreationShouldCreateMainBlock() {
        val transactions = createTransactions()
        val genesisBlock = createGenesisBlock()
        val nodeKey = HashUtils.bytesToHexString("pubKey".toByteArray())
        genesisBlock.activeDelegateKeys = setOf(nodeKey, nodeKey)
        val event = BlockCreationEvent(transactions)

        given(keyHolder.getPublicKey()).willReturn("pubKey".toByteArray())
        given(keyHolder.getPrivateKey()).willReturn("prvKey".toByteArray())
        given(blockService.getLastGenesis()).willReturn(genesisBlock)
        given(signatureManager.sign(any(ByteArray::class.java), any(ByteArray::class.java))).willReturn("sign")

        processor.fireBlockCreation(event)
    }

    private fun createPendingBlock(block: Block): PendingBlock {
        return PendingBlock(
            block,
            SignaturePublicKeyPair("sign", "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719")
        )
    }

    private fun createMainBlock() = MainBlock(
        "454ebbef16f93d174ab0e5e020f8ab80f2cf117e1b6beeeae3151bc87e99f081",
        123,
        "prev_block_hash",
        "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
        1512345678L,
        "signature",
        createTransactions()
    )

    private fun createGenesisBlock() = GenesisBlock(
        "454ebbef16f93d174ab0e5e020f8ab80f2cf117e1b6beeeae3151bc87e99f081",
        123,
        "prev_block_hash",
        "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
        1512345678L,
        "signature",
        1,
        setOf("delegate1", "delegate2", "delegate3")
    )

    private fun createTransactions() = listOf(
        Transaction(
            "transaction_hash1",
            1000,
            1500000000L,
            "recipient_key1",
            "sender_key1",
            "signature1",
            "send_address",
            "recip_address"
        ),
        Transaction(
            "transaction_hash2",
            1002,
            1500000002L,
            "recipient_ke2",
            "sender_key2",
            "signature2",
            "send_address",
            "recip_address"
        )
    )
}
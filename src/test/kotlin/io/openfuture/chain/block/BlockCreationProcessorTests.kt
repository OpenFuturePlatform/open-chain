package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.domain.block.BlockCreationEvent
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
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
        val hashAsBytes = ByteUtils.fromHexString(block.hash)
        val keyAsBytes = ByteUtils.fromHexString(pendingBlock.signature.publicKey)

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
        val hashAsBytes = ByteUtils.fromHexString(block.hash)
        val keyAsBytes = ByteUtils.fromHexString(pendingBlock.signature.publicKey)

        given(blockValidationService.isValid(block)).willReturn(true)
        given(signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)).willReturn(true)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockSignatureIsAlreadyExists() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)
        val hashAsBytes = ByteUtils.fromHexString(block.hash)
        val keyAsBytes = ByteUtils.fromHexString(pendingBlock.signature.publicKey)

        given(blockValidationService.isValid(block)).willReturn(true)
        given(signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)).willReturn(true)
        given(signatureCollector.addBlockSignature(pendingBlock)).willReturn(false)

        processor.approveBlock(pendingBlock)
    }

    @Test
    fun fireBlockCreationShouldCreateMainBlock() {
        val transactions = createTransactions()
        val genesisBlock = createGenesisBlock()
        val nodeKey = ByteUtils.toHexString("pubKey".toByteArray())
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
        123,
        "prev_block_hash",
        "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
        1512345678L,
        "signature",
        createTransactions()
    )

    private fun createGenesisBlock() = GenesisBlock(
        123,
        "prev_block_hash",
        1512345678L,
        "signature",
        1,
        setOf("delegate1", "delegate2", "delegate3")
    )

    private fun createTransactions(): MutableList<BaseTransaction> = mutableListOf(
        VoteTransaction(
            1500000000L,
            1000.0,
            "recipient_key",
            "recipient_address",
            "sender_key",
            "sender_address",
            "sender_signature",
            "hash",
            1,
            "delegate_host",
            9999
        ),
        VoteTransaction(
            1500000001L,
            1002.0,
            "recipient_key2",
            "recipient_address2",
            "sender_key2",
            "sender_address2",
            "sender_signature2",
            "hash2",
            2,
            "delegate_host2",
            11999
        )
    )
}
package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import io.openfuture.chain.service.DelegateService
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

class BlockCreationProcessorTests: ServiceTests() {

    @Mock private lateinit var blockService: BlockService<Block>
    @Mock private lateinit var genesisBlockService: BlockService<GenesisBlock>
    @Mock private lateinit var baseTransactionService: BaseTransactionService<BaseTransaction>
    @Mock private lateinit var signatureCollector: SignatureCollector
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var blockValidationService: BlockValidationProvider
    @Mock private lateinit var consensusService: ConsensusService
    @Mock private lateinit var clock: NodeClock
    @Mock private lateinit var delegateService: DelegateService
    @Mock private lateinit var properties: NodeProperties
    @Mock private lateinit var consensusProperties: ConsensusProperties
    @Mock private lateinit var timeSlot: TimeSlot
    @Mock private lateinit var scheduler: ThreadPoolTaskScheduler

    private lateinit var processor: BlockCreationProcessor


    @Before
    fun init() {
        val block = createMainBlock()
        given(blockService .getLast()).willReturn(block)
        processor = BlockCreationProcessor(blockService, genesisBlockService, baseTransactionService,
            signatureCollector, keyHolder, blockValidationService, consensusService, clock, delegateService, properties,
            consensusProperties, timeSlot, scheduler)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockIsInvalid() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfSignatureVerificationFailed() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockSignatureIsAlreadyExists() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)

        processor.approveBlock(pendingBlock)
    }

    @Test
    fun fireBlockCreationShouldCreateMainBlock() {
        val genesisBlock = createGenesisBlock()
        val delegate = Delegate("public_key", "host", 1234)
        val transactions = createTransactions()
        genesisBlock.activeDelegates = setOf(delegate)

        given(properties.host).willReturn("host")
        given(properties.port).willReturn(1234)
        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        given(keyHolder.getPrivateKey()).willReturn("private_key".toByteArray())
        given(keyHolder.getPublicKey()).willReturn("public_key".toByteArray())
        given(baseTransactionService.getPendingFirstWithLimit(any(Int::class.java))).willReturn(transactions)
        given(consensusProperties.blockCapacity).willReturn(transactions.size)

        processor.fireBlockCreation()

        verify(keyHolder, times(2)).getPrivateKey()
        verify(keyHolder, times(2)).getPublicKey()
    }

    private fun createPendingBlock(block: Block): PendingBlock {
        return PendingBlock(
            block,
            Signature("sign", "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719")
        )
    }

    private fun createMainBlock() = MainBlock(
        HashUtils.fromHexString("529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"),
        123,
        "prev_block_hash",
        "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
        1512345678L,
        HashUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"),
        createTransactions()
    )

    private fun createGenesisBlock() = GenesisBlock(
        HashUtils.fromHexString("529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"),
        123,
        "prev_block_hash",
        1512345678L,
        HashUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"),
        1,
        setOf(Delegate("public_key", "host1", 1234), Delegate("public_key2", "host2", 1234), Delegate("public_key3", "host3", 1234))
    )

    private fun createTransactions(): MutableSet<BaseTransaction> = mutableSetOf(
        VoteTransaction(
            1500000000L,
            1000.0,
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
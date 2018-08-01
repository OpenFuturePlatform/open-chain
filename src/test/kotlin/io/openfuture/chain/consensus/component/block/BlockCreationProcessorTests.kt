package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.consensus.model.dto.block.BlockSignature
import io.openfuture.chain.consensus.model.dto.block.PendingBlock
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.ConsensusService
import io.openfuture.chain.consensus.service.DelegateService
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.consensus.validation.BlockValidationProvider
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.UCommonTransactionService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.node.NodeClock
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.times
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.springframework.scheduling.TaskScheduler

class BlockCreationProcessorTests : ServiceTests() {

    @Mock private lateinit var commonTransactionService: UCommonTransactionService
    @Mock private lateinit var commonBlockService: CommonBlockService
    @Mock private lateinit var genesisBlockService: GenesisBlockService
    @Mock private lateinit var signatureCollector: SignatureCollector
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var blockValidationProvider: BlockValidationProvider
    @Mock private lateinit var consensusService: ConsensusService
    @Mock private lateinit var clock: NodeClock
    @Mock private lateinit var delegateService: DelegateService
    @Mock private lateinit var consensusProperties: ConsensusProperties
    @Mock private lateinit var timeSlot: TimeSlot
    @Mock private lateinit var scheduler: TaskScheduler

    private lateinit var processor: BlockCreationProcessor


    @Before
    fun init() {
        val block = createMainBlock()
        given(commonBlockService.getLast()).willReturn(block)
        processor = BlockCreationProcessor(commonTransactionService, commonBlockService, genesisBlockService,
            signatureCollector, keyHolder, blockValidationProvider, consensusService, clock, delegateService,
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
    fun fireBlockCreationShouldCreateGenesisBlock() {
        val genesisBlock = createGenesisBlock()
        val delegate = Delegate("7075626c69635f6b6579", "host", 1234)
        val transactions = createTransactions()
        genesisBlock.activeDelegates = setOf(delegate)

        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        given(keyHolder.getPrivateKey()).willReturn("private_key".toByteArray())
        given(keyHolder.getPublicKey()).willReturn("public_key".toByteArray())
        given(commonTransactionService.getAll()).willReturn(transactions)

        processor.fireBlockCreation()

        verify(keyHolder, times(2)).getPrivateKey()
        verify(keyHolder, times(3)).getPublicKey()
    }

    private fun createPendingBlock(block: BaseBlock): PendingBlock {
        return PendingBlock(
            block,
            BlockSignature("sign", "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719")
        )
    }

    private fun createMainBlock() = MainBlock(
        123,
        "prev_block_hash",
        1512345678L,
        10,
        "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4",
        "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
        createTransactions().map { it.toConfirmed() }.toMutableSet()
    )

    private fun createGenesisBlock() = GenesisBlock(
        123,
        "prev_block_hash",
        1512345678L,
        10,
        "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6",
        1,
        setOf(Delegate("public_key", "host1", 1234), Delegate("public_key2", "host2", 1234), Delegate("public_key3", "host3", 1234))
    )

    private fun createTransactions(): MutableSet<UTransaction> = mutableSetOf(
        UVoteTransaction(
            1500000000L,
            1000,
            10,
            "recipient_address",
            "sender_key",
            "sender_address",
            "sender_signature",
            "hash",
            1,
            "delegate_key"
        ),
        UVoteTransaction(
            1500000001L,
            1002,
            10,
            "recipient_address2",
            "sender_key2",
            "sender_address2",
            "sender_signature2",
            "hash2",
            2,
            "delegate_key2"
        )
    )

}
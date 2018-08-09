package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mock
import org.mockito.Mockito.times
import org.springframework.context.annotation.Import

@Import(ConsensusProperties::class)
class BlockProductionSchedulerTests : ServiceTests() {

    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var epochService: EpochService
    @Mock private lateinit var blockService: BlockService
    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var genesisBlockService: GenesisBlockService
    @Mock private lateinit var pendingBlockHandler: PendingBlockHandler
    @Mock private lateinit var clock: NodeClock

    private lateinit var consensusProperties: ConsensusProperties

    private lateinit var blockProductionScheduler: BlockProductionScheduler


    @Before
    fun setUp() {
        consensusProperties = ConsensusProperties()
        consensusProperties.epochHeight = 10
        given(epochService.getEpochEndTime()).willReturn(1L)
        blockProductionScheduler = BlockProductionScheduler(
            keyHolder,
            epochService,
            blockService,
            mainBlockService,
            genesisBlockService,
            consensusProperties,
            pendingBlockHandler,
            clock
        )
    }

    @Test
    fun initShouldCreateGenesisBlock() {
        val delegate = Delegate("publicKey", "address", 1)
        val mainBlockPayload = MainBlockPayload("merkleHash")
        val genesisBlockPayload = GenesisBlockPayload(
            1L, listOf()
        )
        val mainBlock = MainBlock(
            1L,
            11L,
            "previousHash",
            3L,
            "hash",
            "signature",
            "publicKey",
            mainBlockPayload
        )
        val genesisBlock = GenesisBlock(
            1L,
            2L,
            "previousHash2",
            3L,
            "hash2",
            "signature",
            "publicKey2",
            genesisBlockPayload
        )
        val genesisBlockMessage = GenesisBlockMessage(genesisBlock)

        given(epochService.getSlotNumber(any(Long::class.java))).willReturn(1L, 2L)
        given(epochService.isInIntermission(any(Long::class.java))).willReturn(true, false)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate)
        given(blockService.getLast()).willReturn(mainBlock)
        given(genesisBlockService.create()).willReturn(genesisBlockMessage)

        blockProductionScheduler.init()

        Thread.sleep(500)
        verify(genesisBlockService, times(1)).add(genesisBlockMessage)
    }

    @Test
    fun initShouldCreateMainBlock() {
        val delegate = Delegate("publicKey", "address", 1)
        val mainBlockPayload = MainBlockPayload("merkleHash")
        val mainBlock = MainBlock(
            1L,
            2L,
            "previousHash",
            3L,
            "hash",
            "signature",
            "publicKey",
            mainBlockPayload
        )
        val mainBlockMessage = PendingBlockMessage(mainBlock, listOf(), listOf(), listOf())

        given(epochService.getSlotNumber(any(Long::class.java))).willReturn(1L, 2L)
        given(epochService.isInIntermission(any(Long::class.java))).willReturn(true, false)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate)
        given(blockService.getLast()).willReturn(mainBlock)
        given(mainBlockService.create()).willReturn(mainBlockMessage)
        given(keyHolder.getPublicKey()).willReturn("publicKey")

        blockProductionScheduler.init()

        Thread.sleep(500)

        verify(pendingBlockHandler, times(1)).addBlock(mainBlockMessage)
    }

}

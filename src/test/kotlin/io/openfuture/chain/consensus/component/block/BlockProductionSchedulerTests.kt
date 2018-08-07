package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.component.node.NodeClock
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class BlockProductionSchedulerTests : ServiceTests() {

    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var epochService: EpochService
    @Mock private lateinit var blockService: BlockService
    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var genesisBlockService: GenesisBlockService
    @Mock private lateinit var consensusProperties: ConsensusProperties
    @Mock private lateinit var pendingBlockHandler: PendingBlockHandler
    @Mock private lateinit var clock: NodeClock

    private lateinit var blockProductionScheduler: BlockProductionScheduler

    @Before
    fun setUp() {
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
    fun testInit() {
        given(epochService.getSlotNumber()).willReturn(1L, 2L)
        given(epochService.isInTimeSlot(any(Long::class.java))).willReturn(false, true)

        blockProductionScheduler.init()

        Thread.sleep(1000)
    }

}

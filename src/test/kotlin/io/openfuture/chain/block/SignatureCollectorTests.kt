package io.openfuture.chain.block

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BlockService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class SignatureCollectorTests : ServiceTests() {

    @Mock private lateinit var mainBlockService: BlockService<MainBlock>
    @Mock private lateinit var genesisBlockService: BlockService<GenesisBlock>
    @Mock private lateinit var properties: ConsensusProperties
    @Mock private lateinit var timeSlot: TimeSlot
    @Mock private lateinit var clock: NodeClock

    private lateinit var signatureCollector: SignatureCollector

    @Before
    fun setUp() {
        given(properties.timeSlotDuration).willReturn(6000L)

        signatureCollector = SignatureCollector(mainBlockService, genesisBlockService, properties, timeSlot, clock)
    }

    @Test
    fun setPendingBlockShouldAddPendingBlock() {
        val generatedBlock = createGenesisPendingBlock()

        signatureCollector.setPendingBlock(generatedBlock)
    }

    @Test
    fun addBlockSignatureShouldReturnTrue() {
        val signatureBlock = createGenesisPendingBlock()
        signatureCollector.setPendingBlock(signatureBlock)

        val result = signatureCollector.addSignatureBlock(signatureBlock)

        assertThat(result).isTrue()
    }

    @Test
    fun addBlockSignatureShouldReturnFalse() {
        val signatureBlock = createGenesisPendingBlock()
        val anotherSignatureBlock = createGenesisPendingBlock()

        signatureCollector.setPendingBlock(anotherSignatureBlock)

        val result = signatureCollector.addSignatureBlock(signatureBlock)

        assertThat(result).isTrue()
    }

    @Test
    fun applyBlockShouldReturnWithoutApply() {
        given(timeSlot.getEpochTime()).willReturn(1L)

        signatureCollector.applyBlock()
    }

    @Test
    fun applyGenesisBlockShouldApplyBlock() {
        val genesisBlock = GenesisBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            1512345678L,
            ByteArray(1),
            1,
            setOf()
        )

        given(clock.networkTime()).willReturn(10000L)
        given(timeSlot.getEpochTime()).willReturn(1L)
        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        val signatureBlock = createGenesisPendingBlock()
        signatureCollector.setPendingBlock(signatureBlock)
        signatureCollector.addSignatureBlock(signatureBlock)

        signatureCollector.applyBlock()
    }

    @Test
    fun applyMainBlockShouldApplyBlock() {
        val genesisBlock = GenesisBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            1512345678L,
            ByteArray(1),
            1,
            setOf()
        )

        given(clock.networkTime()).willReturn(10000L)
        given(timeSlot.getEpochTime()).willReturn(1L)
        given(genesisBlockService.getLast()).willReturn(genesisBlock)
        val signatureBlock = createMainPendingBlock()
        signatureCollector.setPendingBlock(signatureBlock)
        signatureCollector.addSignatureBlock(signatureBlock)

        signatureCollector.applyBlock()
    }

    private fun createMainPendingBlock(): PendingBlock {
        val block = MainBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            ByteArray(1),
            mutableListOf()
        )
        val signature = Signature("value", "public_key")
        return PendingBlock(block, signature)
    }

    private fun createGenesisPendingBlock(): PendingBlock {
        val block = GenesisBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            1512345678L,
            ByteArray(1),
            1,
            setOf()
        )
        val signature = Signature("value", "public_key")
        return PendingBlock(block, signature)
    }

}
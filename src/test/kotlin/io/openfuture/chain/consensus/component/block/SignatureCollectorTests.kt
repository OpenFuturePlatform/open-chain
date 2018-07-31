package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.model.dto.block.BlockSignature
import io.openfuture.chain.consensus.model.dto.block.PendingBlock
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.consensus.service.MainBlockService
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.network.component.node.NodeClock
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.times
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.springframework.scheduling.TaskScheduler
import java.util.*

class SignatureCollectorTests : ServiceTests() {

    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var genesisBlockService: GenesisBlockService
    @Mock private lateinit var properties: ConsensusProperties
    @Mock private lateinit var timeSlot: TimeSlot
    @Mock private lateinit var clock: NodeClock
    @Mock private lateinit var scheduler: TaskScheduler

    private lateinit var signatureCollector: SignatureCollector


    @Before
    fun setUp() {
        given(clock.networkTime()).willReturn(10000L)
        given(properties.timeSlotDuration).willReturn(6000L)

        signatureCollector = SignatureCollector(mainBlockService, genesisBlockService, properties, timeSlot, clock,
            scheduler)
    }

    @Test
    fun setPendingBlockShouldAddPendingBlock() {
        val generatedBlock = createGenesisPendingBlock()

        signatureCollector.setPendingBlock(generatedBlock)

        verify(scheduler, times(1)).schedule(any(Runnable::class.java), any(Date::class.java))
    }

    @Test
    fun addBlockSignatureShouldReturnTrue() {
        val signatureBlock = createGenesisPendingBlock()
        signatureCollector.setPendingBlock(signatureBlock)

        val result = signatureCollector.addBlockSignature(signatureBlock)

        assertThat(result).isTrue()
    }

    @Test
    fun addBlockSignatureShouldReturnFalse() {
        val signatureBlock = createGenesisPendingBlock()
        signatureBlock.block.hash = "hash is changed"
        val anotherSignatureBlock = createGenesisPendingBlock()

        signatureCollector.setPendingBlock(anotherSignatureBlock)

        val result = signatureCollector.addBlockSignature(signatureBlock)

        assertThat(result).isFalse()
    }

    @Test
    fun applyBlockShouldReturnWithoutApply() {
        signatureCollector.setPendingBlock(createGenesisPendingBlock())

        signatureCollector.applyBlock()

        verify(genesisBlockService, times(0)).getLast()
    }

    @Test
    fun applyGenesisBlockShouldApplyBlock() {
        val signatureBlock = createGenesisPendingBlock()
        signatureCollector.setPendingBlock(signatureBlock)
        signatureCollector.addBlockSignature(signatureBlock)

        given(genesisBlockService.getLast()).willReturn(signatureBlock.block as GenesisBlock)
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(Block::class.java))).willReturn(true, false)

        signatureCollector.applyBlock()

        verify(genesisBlockService, times(1)).save(signatureBlock.block as GenesisBlock)
    }

    @Test
    fun applyMainBlockShouldApplyMainBlock() {
        val genesisBlock = createGenesisPendingBlock()
        val signatureBlock = createMainPendingBlock()
        signatureCollector.setPendingBlock(signatureBlock)
        signatureCollector.addBlockSignature(signatureBlock)

        given(genesisBlockService.getLast()).willReturn(genesisBlock.block as GenesisBlock)
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(Block::class.java))).willReturn(true, false)

        signatureCollector.applyBlock()

        verify(mainBlockService, times(1)).save(signatureBlock.block as MainBlock)
    }

    private fun createMainPendingBlock(): PendingBlock {
        val block = MainBlock(
            123,
            "prev_block_hash",
            1512345678L,
            10,
            ByteUtils.toHexString(ByteArray(1)),
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            mutableSetOf()
        ).sign(ByteArray(1))
        val signature = BlockSignature("value", "public_key")
        return PendingBlock(block, signature)
    }

    private fun createGenesisPendingBlock(): PendingBlock {
        val block = GenesisBlock(
            123,
            "prev_block_hash",
            1512345678L,
            10,
            ByteUtils.toHexString(ByteArray(1)),
            1,
            setOf()
        ).sign(ByteArray(1))
        val signature = BlockSignature("value", "public_key")
        return PendingBlock(block, signature)
    }

}
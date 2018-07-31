package io.openfuture.chain.consensus.component.block.validation

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.component.block.TimeSlot
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.consensus.service.MainBlockService
import io.openfuture.chain.consensus.validation.BlockValidationProvider
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.component.node.NodeClock
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import java.lang.IllegalArgumentException

class BlockValidationProviderTests : ServiceTests() {

    @Mock private lateinit var commonBlockService: CommonBlockService
    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var genesisBlockService: GenesisBlockService
    @Mock private lateinit var timeSlot: TimeSlot
    @Mock private lateinit var clock: NodeClock

    private lateinit var blockValidationProvider: BlockValidationProvider

    private val currentTime = System.currentTimeMillis()


    @Before
    fun init() {
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(MainBlock::class.java))).willReturn(true)
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(GenesisBlock::class.java))).willReturn(true)
        blockValidationProvider = BlockValidationProvider(
            commonBlockService, mainBlockService, genesisBlockService, timeSlot, clock)
    }

    @Test
    fun isValidShouldReturnTrueWhenItIsMainBlock() {
        val height = 123L
        val merkleHash = "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719"
        val previousBlock = MainBlock(
            122,
            "previous_hash",
            1510000000L,
            "02f11f42bc8fa42d6ebb457d8f90a0d57194a941df68b132458a24018bc099713b",
            "merkle_hash",
            mutableSetOf(
                VoteTransaction(
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
                VoteTransaction(
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
                ))
        ).sign(ByteUtils.fromHexString("991f15345c6e6ff8dbb5e5dae1f1764ed59e8c98e63b824e2ba20614e0ab2e43"))
        val block = MainBlock(
            height,
            previousBlock.hash,
            currentTime,
            "02f11f42bc8fa42d6ebb457d8f90a0d57194a941df68b132458a24018bc099713b",
            merkleHash,
            mutableSetOf(
                VoteTransaction(
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
                VoteTransaction(
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
        ).sign(ByteUtils.fromHexString("991f15345c6e6ff8dbb5e5dae1f1764ed59e8c98e63b824e2ba20614e0ab2e43"))


        given(mainBlockService.isValid(block)).willReturn(true)
        given(commonBlockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationProvider.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalseWhenItIsMainBlock() {
        val lastBlock = MainBlock(
            123,
            "prev_block_hash",
            1512345678L,
            "02f11f42bc8fa42d6ebb457d8f90a0d57194a941df68b132458a24018bc099713b",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            mutableSetOf()
        ).sign(ByteUtils.fromHexString("991f15345c6e6ff8dbb5e5dae1f1764ed59e8c98e63b824e2ba20614e0ab2e43"))
        val block = MainBlock(
            123,
            "prev_block_hash",
            1512345678L,
            "02f11f42bc8fa42d6ebb457d8f90a0d57194a941df68b132458a24018bc099713b",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            mutableSetOf(
                VoteTransaction(
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
                VoteTransaction(
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
        ).sign(ByteUtils.fromHexString("991f15345c6e6ff8dbb5e5dae1f1764ed59e8c98e63b824e2ba20614e0ab2e43"))


        given(commonBlockService.getLast()).willReturn(lastBlock)

        val isValid = blockValidationProvider.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidShouldReturnTrueWhenItIsGenesisBlock() {
        val previousBlock = GenesisBlock(
            122,
            "previous_hash",
            1510000000L,
            "02f11f42bc8fa42d6ebb457d8f90a0d57194a941df68b132458a24018bc099713b",
            1L,
            setOf()
        ).sign(ByteUtils.fromHexString("991f15345c6e6ff8dbb5e5dae1f1764ed59e8c98e63b824e2ba20614e0ab2e43"))

        val block = GenesisBlock(
            123L,
            previousBlock.hash,
            currentTime,
            "02f11f42bc8fa42d6ebb457d8f90a0d57194a941df68b132458a24018bc099713b",
            2L,
            setOf()
        ).sign(ByteUtils.fromHexString("991f15345c6e6ff8dbb5e5dae1f1764ed59e8c98e63b824e2ba20614e0ab2e43"))

        given(genesisBlockService.isValid(block)).willReturn(true)
        given(commonBlockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationProvider.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalseWhenItIsGenesisBlock() {
        val lastBlock = MainBlock(
            123,
            "prev_block_hash",
            1512345678L,
            "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            mutableSetOf()
        ).sign(ByteArray(1))

        val block = GenesisBlock(
            123,
            "prev_block_hash",
            1512345678L,
            ByteUtils.toHexString(ByteArray(1)),
            1L,
            setOf()
        ).sign(ByteArray(1))

        given(commonBlockService.getLast()).willReturn(lastBlock)

        val isValid = blockValidationProvider.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun isValidShouldThrowIllegalArgumentException() {
        val block = Mockito.mock(Block::class.java)

        blockValidationProvider.isValid(block)
    }

}
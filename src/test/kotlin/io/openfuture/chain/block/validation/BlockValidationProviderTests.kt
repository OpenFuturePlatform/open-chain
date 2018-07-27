package io.openfuture.chain.block.validation

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.service.CommonBlockService
import io.openfuture.chain.service.GenesisBlockService
import io.openfuture.chain.service.MainBlockService
import org.assertj.core.api.Assertions.assertThat
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

    private lateinit var blockValidationService: BlockValidationProvider

    private val currentTime = System.currentTimeMillis()


    @Before
    fun init() {
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(MainBlock::class.java))).willReturn(true)
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(GenesisBlock::class.java))).willReturn(true)
        blockValidationService = BlockValidationProvider(
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
            HashUtils.toHexString(ByteArray(1)),
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
        ).sign<MainBlock>(ByteArray(1))
        val block = MainBlock(
            height,
            previousBlock.hash,
            currentTime,
            "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4",
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
        ).sign<MainBlock>(ByteArray(1))


        given(mainBlockService.isValid(block)).willReturn(true)
        given(commonBlockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalseWhenItIsMainBlock() {
        val lastBlock = MainBlock(
            123,
            "prev_block_hash",
            1512345678L,
            HashUtils.toHexString(ByteArray(1)),
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            mutableSetOf()
        )
        val block = MainBlock(
            123,
            "prev_block_hash",
            1512345678L,
            HashUtils.toHexString(ByteArray(1)),
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
        ).sign<MainBlock>(ByteArray(1))


        given(commonBlockService.getLast()).willReturn(lastBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidShouldReturnTrueWhenItIsGenesisBlock() {
        val previousBlock = GenesisBlock(
            122,
            "previous_hash",
            1510000000L,
            HashUtils.toHexString(ByteArray(1)),
            1L,
            setOf()
        ).sign<GenesisBlock>(ByteArray(1))

        val block = GenesisBlock(
            123L,
            previousBlock.hash,
            currentTime,
            "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4",
            2L,
            setOf()
        ).sign<GenesisBlock>(ByteArray(1))

        given(genesisBlockService.isValid(block)).willReturn(true)
        given(commonBlockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationService.isValid(block)

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
        ).sign<MainBlock>(ByteArray(1))

        val block = GenesisBlock(
            123,
            "prev_block_hash",
            1512345678L,
            HashUtils.toHexString(ByteArray(1)),
            1L,
            setOf()
        ).sign<GenesisBlock>(ByteArray(1))

        given(commonBlockService.getLast()).willReturn(lastBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun isValidShouldThrowIllegalArgumentException() {
        val block = Mockito.mock(Block::class.java)

        blockValidationService.isValid(block)
    }

}
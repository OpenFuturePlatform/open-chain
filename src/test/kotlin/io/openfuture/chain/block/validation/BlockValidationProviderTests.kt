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
import io.openfuture.chain.service.BlockService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import java.lang.IllegalArgumentException

class BlockValidationProviderTests : ServiceTests() {

    @Mock private lateinit var blockService: BlockService<Block>
    @Mock private lateinit var mainBlockService: BlockService<MainBlock>
    @Mock private lateinit var genesisBlockService: BlockService<GenesisBlock>
    @Mock private lateinit var timeSlot: TimeSlot
    @Mock private lateinit var clock: NodeClock

    private lateinit var blockValidationService: BlockValidationProvider

    private val currentTime = System.currentTimeMillis()


    @Before
    fun init() {
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(MainBlock::class.java))).willReturn(true)
        given(timeSlot.verifyTimeSlot(any(Long::class.java), any(GenesisBlock::class.java))).willReturn(true)
        blockValidationService = BlockValidationProvider(
            blockService, mainBlockService, genesisBlockService, timeSlot, clock)
    }

    @Test
    fun isValidShouldReturnTrueWhenItIsMainBlock() {
        val height = 123L
        val prevHash = "aabf4eb7907b9a903ca57c345a7298e3604092be23c2ba0a8de04a99316dd52a"
        val merkleHash = "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719"
        val previousBlock = MainBlock(
            ByteArray(1),
            122,
            "previous_hash",
            "merkle_hash",
            1510000000L,
            ByteArray(1),
            mutableSetOf(
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
        )
        val block = MainBlock(
            HashUtils.fromHexString("529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"),
            height,
            prevHash,
            merkleHash,
            currentTime,
            HashUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"),
            mutableSetOf(
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
        )

        given(mainBlockService.isValid(block)).willReturn(true)
        given(blockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalseWhenItIsMainBlock() {
        val lastBlock = MainBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            ByteArray(1),
            mutableSetOf()
        )
        val block = MainBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            ByteArray(1),
            mutableSetOf(
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
        )

        given(blockService.getLast()).willReturn(lastBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidShouldReturnTrueWhenItIsGenesisBlock() {
        val previousBlock = GenesisBlock(
            ByteArray(1),
            122,
            "previous_hash",
            1510000000L,
            ByteArray(1),
            1L,
            setOf()
        )
        val block = GenesisBlock(
            HashUtils.fromHexString("529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"),
            123L,
            "ca9d1f7613c1977e109a8fe211d019e350c84a5ba6d63e3c9c24eb886f1e4181",
            currentTime,
            HashUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"),
            2L,
            setOf()
        )

        given(genesisBlockService.isValid(block)).willReturn(true)
        given(blockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalseWhenItIsGenesisBlock() {
        val lastBlock = MainBlock(
            HashUtils.fromHexString("529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"),
            123,
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            HashUtils.fromHexString("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"),
            mutableSetOf()
        )
        val block = GenesisBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            1512345678L,
            ByteArray(1),
            1L,
            setOf()
        )

        given(blockService.getLast()).willReturn(lastBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun isValidShouldThrowIllegalArgumentException() {
        val block = Mockito.mock(Block::class.java)

        blockValidationService.isValid(block)
    }

}
package io.openfuture.chain.block.validation

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.DefaultBlockService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
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
        val prevHash = "c78bac60ede7a9d10248ad4373d70b915a1c466e942aadce1f5703ebbb855aa4"
        val merkleHash = "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719"
        val previousBlock = MainBlock(
            ByteArray(1),
            122,
            "previous_hash",
            "merkle_hash",
            1510000000L,
            mutableListOf(
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
            ByteArray(1),
            height,
            prevHash,
            merkleHash,
            currentTime,
            mutableListOf(
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
        val block = MainBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            mutableListOf(
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

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidShouldReturnTrueWhenItIsGenesisBlock() {
        val height = 123L
        val prevHash = "08a6f91ee999e7d8c89861ee5eee401b0fed8ba2a83f7ab32de5f8dae0c06024"
        val previousBlock = GenesisBlock(
            ByteArray(1),
            122,
            "previous_hash",
            1510000000L,
            1L,
            setOf()
        )
        val block = GenesisBlock(
            ByteArray(1),
            height,
            prevHash,
            currentTime,
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
        val block = GenesisBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            1512345678L,
            1L,
            setOf()
        )

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun isValidShouldThrowIllegalArgumentException() {
        val block = Mockito.mock(Block::class.java)

        blockValidationService.isValid(block)
    }

}
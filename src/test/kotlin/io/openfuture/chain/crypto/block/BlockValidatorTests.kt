package io.openfuture.chain.crypto.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.service.BlockService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class BlockValidatorTests : ServiceTests() {

    @Mock private lateinit var blockService: BlockService

    private lateinit var blockValidator: BlockValidator


    @Before
    fun setUp() {
        blockValidator = BlockValidator(blockService)
    }

    @Test
    fun isValidShouldReturnTrue() {
        val previouseBlock = Block(
            "prev_block_hash",
            122,
            "prev_signature",
            "previous_hash",
            "merkle_hash",
            1510000000L,
            setOf(
                Transaction(
                    "prev_transaction_hash1",
                    2000,
                    1500000000L,
                    "prev_recipient_key1",
                    "prev_sender_key1",
                    "prev_signature1"
                )
            )
        )

        val block = Block(
            "block_hash",
            123,
            "signature",
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            setOf(
                Transaction(
                    "transaction_hash1",
                    1000,
                    1500000000L,
                    "recipient_key1",
                    "sender_key1",
                    "signature1"
                ),
                Transaction(
                    "transaction_hash2",
                    1002,
                    1500000002L,
                    "recipient_ke2",
                    "sender_key2",
                    "signature2"
                )
            )
        )

        given(blockService.getLast()).willReturn(previouseBlock)

        val isValid = blockValidator.isValid(block)

        assertThat(isValid).isTrue()
    }
}
package io.openfuture.chain.block

import io.openfuture.chain.block.validation.MainBlockValidator
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class MainBlockValidatorTests : ServiceTests() {

    private lateinit var mainBlockValidator: MainBlockValidator

    @Before
    fun setUp() {
        mainBlockValidator = MainBlockValidator()
    }

    @Test
    fun isValidShouldReturnTrue() {
        val block = MainBlock(
            "454ebbef16f93d174ab0e5e020f8ab80f2cf117e1b6beeeae3151bc87e99f081",
            123,
            "prev_block_hash",
            "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
            1512345678L,
            "signature",
            listOf(
                Transaction(
                    "transaction_hash1",
                    1000,
                    1500000000L,
                    "recipient_key1",
                    "sender_key1",
                    "signature1",
                    "send_address",
                    "recip_address"
                ),
                Transaction(
                    "transaction_hash2",
                    1002,
                    1500000002L,
                    "recipient_ke2",
                    "sender_key2",
                    "signature2",
                    "send_address",
                    "recip_address"
                )
            )
        )

        val isBlockValid = mainBlockValidator.isValid(block)

        Assertions.assertThat(isBlockValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalse() {
        val block = MainBlock(
            "454ebbef16f93d174ab0e5e020f8ab80f2cf117e1b6beeeae3151bc87e99f081",
            123,
            "prev_block_hash",
            "0000000000000000000000000000000000000000000000000000000000000000",
            1512345678L,
            "signature",
            listOf(
                Transaction(
                    "transaction_hash1",
                    1000,
                    1500000000L,
                    "recipient_key1",
                    "sender_key1",
                    "signature1",
                    "send_address",
                    "recip_address"
                ),
                Transaction(
                    "transaction_hash2",
                    1002,
                    1500000002L,
                    "recipient_ke2",
                    "sender_key2",
                    "signature2",
                    "send_address",
                    "recip_address"
                )
            )
        )

        val isBlockValid = mainBlockValidator.isValid(block)

        Assertions.assertThat(isBlockValid).isFalse()
    }
}
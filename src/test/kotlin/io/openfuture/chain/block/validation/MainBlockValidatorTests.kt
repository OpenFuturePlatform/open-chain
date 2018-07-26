package io.openfuture.chain.block.validation

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
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
            123,
            "prev_block_hash",
            "04d44ab3ecdf48273794f2bd85f185480c68415ad9595c8da07aadeac55b0a7e",
            1512345678L,
            mutableListOf(
                VoteTransaction(
                    1500000000L,
                    1000.0,
                    10.0,
                    "recipient_address",
                    "sender_key",
                    "sender_address",
                    1,
                    "delegate_host",
                    9999
                ).sign(ByteArray(1)),
                VoteTransaction(
                    1500000001L,
                    1002.0,
                    10.0,
                    "recipient_address2",
                    "sender_key2",
                    "sender_address2",
                    2,
                    "delegate_host2",
                    11999
                ).sign(ByteArray(1))
            )
        )

        val isBlockValid = mainBlockValidator.isValid(block)

        Assertions.assertThat(isBlockValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalse() {
        val block = MainBlock(
            123,
            "prev_block_hash",
            "0000000000000000000000000000000000000000000000000000000000000000",
            1512345678L,
            mutableListOf(
                VoteTransaction(
                    1500000000L,
                    1000.0,
                    10.0,
                    "recipient_address",
                    "sender_key",
                    "sender_address",
                    1,
                    "delegate_host",
                    9999
                ).sign(ByteArray(1)),
                VoteTransaction(
                    1500000001L,
                    1002.0,
                    10.0,
                    "recipient_address2",
                    "sender_key2",
                    "sender_address2",
                    2,
                    "delegate_host2",
                    11999
                ).sign(ByteArray(1))
            )
        )

        val isBlockValid = mainBlockValidator.isValid(block)

        Assertions.assertThat(isBlockValid).isFalse()
    }
}
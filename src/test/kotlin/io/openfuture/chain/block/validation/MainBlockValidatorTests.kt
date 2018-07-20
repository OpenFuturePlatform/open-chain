package io.openfuture.chain.block.validation

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.CoinBaseTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.ConsensusProperties
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class MainBlockValidatorTests : ServiceTests() {

    @Mock private lateinit var properties: ConsensusProperties

    private lateinit var mainBlockValidator: MainBlockValidator

    companion object {
        private const val GENESIS_ADDRESS = "0x00000"
    }


    @Before
    fun setUp() {
        mainBlockValidator = MainBlockValidator(properties)

        given(properties.genesisAddress).willReturn(GENESIS_ADDRESS)
    }

    @Test
    fun isValidShouldReturnTrue() {
        val block = MainBlock(
            ByteArray(1),
            123,
            "prev_block_hash",
            "0e09773036394004cb8c340e639a89d7a18e924e8a3d048b49864aeb017e07a0",
            1512345678L,
            mutableListOf(
                CoinBaseTransaction(
                    1500000000L,
                    20.0,
                    0.0,
                    "recipient_address",
                    "sender_key",
                    GENESIS_ADDRESS,
                    "hash",
                    "sender_signature"
                ),
                VoteTransaction(
                    1500000001L,
                    1002.0,
                    10.0,
                    "recipient_address2",
                    "sender_key2",
                    "sender_address2",
                    2,
                    "delegate_host2",
                    11999,
                    "hash2",
                    "sender_signature2"
                )
            )
        )

        val isBlockValid = mainBlockValidator.isValid(block)


        assertThat(isBlockValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalse() {
        val block = MainBlock(
            ByteArray(1),
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
                    9999,
                    "hash",
                    "sender_signature"
                ),
                VoteTransaction(
                    1500000001L,
                    1002.0,
                    10.0,
                    "recipient_address2",
                    "sender_key2",
                    "sender_address2",
                    2,
                    "delegate_host2",
                    11999,
                    "hash2",
                    "sender_signature2"
                )
            )
        )

        val isBlockValid = mainBlockValidator.isValid(block)

        assertThat(isBlockValid).isFalse()
    }

}
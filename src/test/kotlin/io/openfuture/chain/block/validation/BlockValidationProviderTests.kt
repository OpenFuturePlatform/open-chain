package io.openfuture.chain.block.validation

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.entity.*
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BlockService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.context.ApplicationContext

class BlockValidationProviderTests : ServiceTests() {

    @Mock private lateinit var blockService: BlockService
    @Mock private lateinit var applicationContext: ApplicationContext
    @Mock private lateinit var blockValidator: BlockValidator
    @Mock private lateinit var properties: ConsensusProperties

    private lateinit var blockValidationService: BlockValidationProvider

    private val currentTime = System.currentTimeMillis()


    @Before
    fun init() {
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

        val validators = HashMap<String, BlockValidator>()
        validators[""] = blockValidator
        given(blockService.getLast()).willReturn(previousBlock)
        given(properties.timeSlotDuration).willReturn(6000)
        given(blockValidator.getTypeId()).willReturn(BlockType.MAIN.id)
        given(applicationContext.getBeansOfType(BlockValidator::class.java)).willReturn(validators)
        blockValidationService = BlockValidationProvider(applicationContext, blockService, properties)
        blockValidationService.init()
        blockValidationService.setEpochTime(currentTime)
    }

    @Test
    fun isValidShouldReturnTrue() {
        val height = 123L
        val prevHash = "c78bac60ede7a9d10248ad4373d70b915a1c466e942aadce1f5703ebbb855aa4"
        val merkleHash = "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719"

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

        given(blockValidator.isValid(any(Block::class.java))).willReturn(true)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalse() {
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

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

}
package io.openfuture.chain.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockVersion
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.service.BlockService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.context.ApplicationContext

class BlockValidationServiceTests : ServiceTests() {

    @Mock private lateinit var blockService: BlockService

    @Mock private lateinit var applicationContext: ApplicationContext

    @Mock private lateinit var blockValidator: BlockValidator

    private lateinit var blockValidationService: BlockValidationService


    @Before
    fun setUp() {
        val validators = HashMap<String, BlockValidator>()
        validators[""] = blockValidator
        given(blockValidator.getVersion()).willReturn(BlockVersion.MAIN.version)
        given(applicationContext.getBeansOfType(BlockValidator::class.java)).willReturn(validators)
        blockValidationService = BlockValidationService(blockService, applicationContext, 1000)
        blockValidationService.init()
    }

    @Test
    fun isValidShouldReturnTrue() {
        val previousBlock = MainBlock(
            "prev_block_hash",
            122,
            "previous_hash",
            "merkle_hash",
            1510000000L,
            "prev_signature",
            listOf(
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

        given(blockValidator.isValid(any(Block::class.java))).willReturn(true)
        given(blockService.getLast()).willReturn(previousBlock)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidShouldReturnFalse() {
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

        given(blockValidator.isValid(any(Block::class.java))).willReturn(false)

        val isValid = blockValidationService.isValid(block)

        assertThat(isValid).isFalse()
    }

}
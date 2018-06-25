package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.block.MinedBlockDto
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.BlockRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*


internal class DefaultBlockServiceTest: ServiceTests() {

    @Mock private lateinit var pageable: Pageable
    @Mock private lateinit var repository: BlockRepository
    @Mock private lateinit var transactionService: TransactionService

    private lateinit var service: BlockService

    @Before
    fun setUp() {
        service = DefaultBlockService(repository, transactionService)
    }

    @Test
    fun count() {
        val expectedCount = 1L
        given(repository.count()).willReturn(expectedCount)
        val actualCount = service.count()
        assertThat(actualCount).isEqualTo(expectedCount)
    }

    @Test
    fun getAll() {
        val block = createBlock()
        val expectedBlockPages = PageImpl(Collections.singletonList(block), pageable, 1)

        given(repository.findAll(pageable)).willReturn(expectedBlockPages)

        val actualBlockPages = service.getAll(pageable)

        assertThat(actualBlockPages).isEqualTo(expectedBlockPages)
    }

    @Test
    fun getLast() {
        val expectedBlock = createBlock()
        given(repository.findFirstByOrderByOrderNumberDesc()).willReturn(expectedBlock)
        val actualBlock = service.getLast()
        assertThat(actualBlock).isEqualTo(expectedBlock)
    }

    @Test
    fun save() {
        val transactionDto = createTransactionDto()
        val minedBlockDto = createMinedBlockDto()
        minedBlockDto.transactions.add(transactionDto)

        val expectedBlock = Block.of(minedBlockDto)
        val expectedTransaction = Transaction.of(expectedBlock, transactionDto)
        expectedBlock.transactions = mutableListOf(expectedTransaction)

        given(repository.save(any(Block::class.java))).willReturn(expectedBlock)
        given(transactionService.save(expectedBlock, transactionDto)).willReturn(expectedTransaction)

        val actualBlock = service.save(minedBlockDto)
        assertThat(actualBlock).isEqualTo(expectedBlock)
    }

    private fun createMinedBlockDto(): MinedBlockDto {
        return MinedBlockDto(0, 0, "previousHash", mutableListOf(),
                "merkleHash", BlockHash(0, "hash"), "nodePublicKey",
                "nodeSignature")
    }

    private fun createTransactionDto(): TransactionDto {
        return TransactionDto(0, 0, "recipientKey", "senderKey",
                "signature")
    }

    private fun createBlock(): Block = Block(1, 0, "previousHash",
            "merkleHash", 0, "hash", "nodeKey", "nodeSignature", mutableListOf())

}

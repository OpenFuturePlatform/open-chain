package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock

internal class DefaultTransactionServiceTest: ServiceTests() {

    @Mock private lateinit var repository: TransactionRepository

    private lateinit var service: TransactionService


    @Before
    fun setUp() {
        service = DefaultTransactionService(repository)
    }

    @Test
    fun save() {
        val transactionDto = createTransactionDto()
        val expectedTransaction = Transaction.of(transactionDto)

        given(repository.save(any(Transaction::class.java))).willReturn(expectedTransaction)

        val actualTransaction = service.add(transactionDto)
        Assertions.assertThat(actualTransaction).isEqualTo(expectedTransaction)
    }

    private fun createTransactionDto(): TransactionDto = TransactionDto(0, 0,
            "recipientKey", "senderKey", "signature", "hash")

}


package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.context.ApplicationEventPublisher

class DefaultTransactionServiceTests : ServiceTests() {

    @Mock private lateinit var repository: TransactionRepository
    @Mock private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var service: TransactionService


    @Before
    fun setUp() {
        service = DefaultTransactionService(repository, eventPublisher, 1)
    }

    @Test
    fun saveShouldReturnSavedTransactionTest() {
        val transaction = Transaction("hash", 1, 1L, "recipientKey", "senderKey", "signature", "from", "to")

        given(repository.save(any(Transaction::class.java))).will { invocation -> invocation.arguments[0] }

        val actualTransaction = service.save(transaction)

        assertThat(actualTransaction.hash).isEqualTo(transaction.hash)
        assertThat(actualTransaction.senderKey).isEqualTo(transaction.senderKey)
        assertThat(actualTransaction.recipientKey).isEqualTo(transaction.recipientKey)
    }

}
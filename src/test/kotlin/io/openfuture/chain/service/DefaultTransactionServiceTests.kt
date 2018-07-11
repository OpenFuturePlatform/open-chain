package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class DefaultTransactionServiceTests : ServiceTests() {

    @Mock private lateinit var repository: TransactionRepository

    private lateinit var service: TransactionService


    @Before
    fun setUp() {
        service = DefaultTransactionService(repository)
    }

    @Test
    fun saveShouldReturnSavedTransactionTest() {
        val block = Block(1, 1L, 1L, "previousHash", "hash", "merkleHash", "nodeKey", "nodeSignature")
        val transaction = Transaction(block, "hash", 1, 1L, "recipientKey", "senderKey", "signature", "from", "to")

        given(repository.save(any(Transaction::class.java))).will { invocation -> invocation.arguments[0] }

        val actualTransaction = service.save(transaction)

        assertThat(actualTransaction.block).isEqualTo(block)
        assertThat(actualTransaction.hash).isEqualTo(transaction.hash)
        assertThat(actualTransaction.senderKey).isEqualTo(transaction.senderKey)
        assertThat(actualTransaction.recipientKey).isEqualTo(transaction.recipientKey)
    }

}
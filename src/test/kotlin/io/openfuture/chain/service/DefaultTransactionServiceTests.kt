package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.domain.transaction.TransactionRequest
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
    @Mock private lateinit var blockService: DefaultBlockService

    private lateinit var service: TransactionService


    @Before
    fun setUp() {
        service = DefaultTransactionService(repository, blockService)
    }

    @Test
    fun saveShouldReturnSavedTransactionTest() {
        val request = TransactionRequest(1, 1, 1L, "recipientKey", "senderKey", "signature")
        val block = createBlock()

        given(blockService.get(request.blockId)).willReturn(block)
        given(repository.save(any(Transaction::class.java))).will { invocation -> invocation.arguments[0] }

        val actualTransaction = service.save(request)

        assertThat(actualTransaction.block).isEqualTo(block)
        assertThat(actualTransaction.hash).isEqualTo(request.hash)
        assertThat(actualTransaction.senderKey).isEqualTo(request.senderKey)
        assertThat(actualTransaction.recipientKey).isEqualTo(request.recipientKey)
    }

    @Test
    fun getByRecipientKeyShouldReturnTransactionByRecipientKeyTest() {
        val recipientKey = "recipientKey"
        val transaction = Transaction(createBlock(), "hash", 1, 1L, "recipientKey", "senderKey", "signature")

        given(repository.findByRecipientKey(recipientKey)).willReturn(listOf(transaction))

        val actualTransactions = service.getByRecipientKey(recipientKey)

        assertThat(actualTransactions[0]).isEqualTo(transaction)
    }

    @Test
    fun getBySenderKeyShouldReturnTransactionBySenderKeyTest() {
        val recipientKey = "recipientKey"
        val transaction = Transaction(createBlock(), "hash", 1, 1L, "recipientKey", "senderKey", "signature")

        given(repository.findBySenderKey(recipientKey)).willReturn(listOf(transaction))

        val actualTransactions = service.getBySenderKey(recipientKey)

        assertThat(actualTransactions[0]).isEqualTo(transaction)
    }

    private fun createBlock(): Block = Block(1, 1L, 1L, "previousHash", "hash", "merkleHash", "nodeKey", "nodeSignature")

}
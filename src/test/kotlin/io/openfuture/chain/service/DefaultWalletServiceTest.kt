package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.exception.BalanceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class DefaultWalletServiceTest : ServiceTests() {

    @Mock private lateinit var transactionService: TransactionService

    private lateinit var service: WalletService


    @Before
    fun setUp() {
        service = DefaultWalletService(transactionService)
    }

    @Test
    fun getTotalBalanceShouldReturnBalanceFromAllTransactionsByKeyTest() {
        val key = "key"
        val sentAmount = 5
        val receivedAmount = 10
        val expectedBalance = receivedAmount - sentAmount
        val sentTransactions = createTransactions(sentAmount, "recipientKey", key)
        val receivedTransactions = createTransactions(receivedAmount, "key", "senderKey")

        given(transactionService.getByRecipientKey(key)).willReturn(receivedTransactions)
        given(transactionService.getBySenderKey(key)).willReturn(sentTransactions)

        val actualBalance = service.getBalance(key)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test(expected = BalanceException::class)
    fun getTotalBalanceWhenNegativeBalanceShouldThrowExceptionTest() {
        val key = "key"
        val sentAmount = 10
        val receivedAmount = 5
        val sentTransactions = createTransactions(sentAmount, "recipientKey", key)
        val receivedTransactions = createTransactions(receivedAmount, "key", "senderKey")

        given(transactionService.getByRecipientKey(key)).willReturn(receivedTransactions)
        given(transactionService.getBySenderKey(key)).willReturn(sentTransactions)

        service.getBalance(key)
    }

    private fun createTransactions(amount: Int, senderKey: String, recipientKey: String): List<Transaction> {
        val block = Block(1, 1L, 1L, "previousHash", "hash", "merkleHash", "nodeKey", "nodeSignature")

        return listOf(Transaction(block, "hash", amount, 1L, recipientKey, senderKey, "signature"))
    }

}
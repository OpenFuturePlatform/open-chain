package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.repository.WalletRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import java.util.*

class DefaultWalletServiceTest : ServiceTests() {

    @Mock private lateinit var repository: WalletRepository

    private lateinit var service: WalletService


    @Before
    fun setUp() {
        service = DefaultWalletService(repository)
    }

    @Test
    fun getBalanceShouldReturnBalanceFromAllTransactionsByAddressTest() {
        val address = "address"
        val expectedBalance = 10.0
        val wallet = Wallet(address, expectedBalance)

        given(repository.findOneByAddress(address)).willReturn(wallet)

        val actualBalance = service.getBalance(address)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test
    fun getBalanceWhenNotExistsWalletByAddressShouldReturnDefaultBalanceValueTest() {
        val address = "address"
        val expectedBalance = 0.0

        given(repository.findOneByAddress(address)).willReturn(null)

        val actualBalance = service.getBalance(address)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test
    fun updateByTransactionShouldChangeWalletsBalanceValueTest() {
        val amount = 1.0
        val senderAddress = "senderAddress"
        val recipientAddress = "recipientAddress"

        val transaction = createTransaction(amount, senderAddress, recipientAddress)
        val senderWallet = Wallet(senderAddress, 1.0)
        val recipientWallet = Wallet(recipientAddress, 5.0)

        given(repository.findOneByAddress(senderAddress)).willReturn(senderWallet)
        given(repository.findOneByAddress(recipientAddress)).willReturn(recipientWallet)

        service.updateByTransaction(transaction)

        verify(repository).save(senderWallet.apply { balance += amount })
        verify(repository).save(recipientWallet.apply { balance -= amount })
    }

    private fun createTransaction(amount: Double, senderAddress: String, recipientAddress: String): BaseTransaction {
        val block = MainBlock(ByteArray(1), 1L, "previousHash", "hash", 1L, mutableListOf())

        return VoteTransaction(Date().time, amount, "recipientKey", recipientAddress,
            "senderKey", senderAddress, "signature", "hash", VoteType.FOR.getId(),
            "delegateKey", 1, block)
    }

}
package io.openfuture.chain.consensus.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.repository.WalletRepository
import io.openfuture.chain.core.service.DefaultWalletService
import io.openfuture.chain.core.service.WalletService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify

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
        val expectedBalance = 10L
        val wallet = Wallet(address, expectedBalance)

        given(repository.findOneByAddress(address)).willReturn(wallet)

        val actualBalance = service.getActualBalanceByAddress(address)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test
    fun getBalanceWhenNotExistsWalletByAddressShouldReturnDefaultBalanceValueTest() {
        val address = "address"
        val expectedBalance = 0L

        given(repository.findOneByAddress(address)).willReturn(null)

        val actualBalance = service.getActualBalanceByAddress(address)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test
    fun updateBalanceShouldChangeWalletsBalanceValueTest() {
        val amount = 2L
        val recipientAddress = "recipientAddress"

        val recipientWallet = Wallet(recipientAddress, 5)

        given(repository.findOneByAddress(recipientAddress)).willReturn(recipientWallet)

        service.increaseBalance(recipientAddress, amount)

        verify(repository).save(recipientWallet.apply { balance += amount })
    }

}
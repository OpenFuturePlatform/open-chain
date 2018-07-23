package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.WalletRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify

class DefaultWalletServiceTest : ServiceTests() {

    @Mock private lateinit var repository: WalletRepository
    @Mock private lateinit var properties: ConsensusProperties

    private lateinit var service: WalletService


    @Before
    fun setUp() {
        service = DefaultWalletService(repository, properties)
    }

    @Test
    fun getBalanceShouldReturnBalanceFromAllTransactionsByAddressTest() {
        val address = "address"
        val expectedBalance = 10L
        val wallet = Wallet(address, expectedBalance)

        given(repository.findOneByAddress(address)).willReturn(wallet)

        val actualBalance = service.getBalanceByAddress(address)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test
    fun getBalanceWhenNotExistsWalletByAddressShouldReturnDefaultBalanceValueTest() {
        val address = "address"
        val expectedBalance = 0L

        given(repository.findOneByAddress(address)).willReturn(null)

        val actualBalance = service.getBalanceByAddress(address)

        assertThat(actualBalance).isEqualTo(expectedBalance)
    }

    @Test
    fun updateBalanceShouldChangeWalletsBalanceValueTest() {
        val amount = 1L
        val senderAddress = "senderAddress"
        val recipientAddress = "recipientAddress"

        val senderWallet = Wallet(senderAddress, 1)
        val recipientWallet = Wallet(recipientAddress, 5)

        given(repository.findOneByAddress(senderAddress)).willReturn(senderWallet)
        given(repository.findOneByAddress(recipientAddress)).willReturn(recipientWallet)

        service.updateBalance(senderAddress, recipientAddress, amount)

        verify(repository).save(senderWallet.apply { balance += amount })
        verify(repository).save(recipientWallet.apply { balance -= amount })
    }

}
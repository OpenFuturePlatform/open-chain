package io.openfuture.chain.rpc.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import io.openfuture.chain.rpc.domain.crypto.key.RestoreRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

@WebFluxTest(AccountController::class)
class AccountControllerTests : ControllerTests() {

    @MockBean
    private lateinit var cryptoService: CryptoService

    @MockBean
    private lateinit var nodeClock: NodeClock

    @MockBean
    private lateinit var nodeProperties: NodeProperties

    @MockBean
    private lateinit var walletService: WalletService


    @Test
    fun validateAddressShouldReturnAddressAndStatusOk() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36e"
        val request = ValidateAddressRequest(address)

        webClient.post().uri("/rpc/accounts/wallets/validateAddress")
            .body(Mono.just(request), ValidateAddressRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(ValidateAddressRequest::class.java).isEqualTo<Nothing>(request)
    }

    @Test
    fun validateAddressShouldReturnStatusBadRequest() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36eaaaa"
        val request = ValidateAddressRequest(address)

        webClient.post().uri("/rpc/accounts/wallets/validateAddress")
            .body(Mono.just(request), ValidateAddressRequest::class.java)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun doRestoreShouldReturnRootAccountInfoWhenSeedPhraseSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val masterKeys = ExtendedKey.root(ByteArray(0))
        val defaultWalletKeys = ExtendedKey.root(ByteArray(1))
        val expectedAccount = AccountDto(seedPhrase, KeyDto(masterKeys.ecKey), WalletDto(defaultWalletKeys.ecKey))

        given(cryptoService.getMasterKey(expectedAccount.seedPhrase)).willReturn(masterKeys)
        given(cryptoService.getDefaultDerivationKey(masterKeys)).willReturn(defaultWalletKeys)

        val result = webClient.post().uri("/rpc/accounts/doRestore")
            .body(Mono.just(RestoreRequest(expectedAccount.seedPhrase)), RestoreRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(ObjectMapper().writeValueAsString(expectedAccount))
    }

    @Test
    fun doDeriveReturnDerivationKeyWhenSeedPhraseDerivationPathAndSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val derivationKeyRequest = DerivationKeyRequest(seedPhrase, derivationPath)
        val masterKeys = ExtendedKey.root(ByteArray(0))
        val defaultWalletKeys = ExtendedKey.root(ByteArray(1))
        val expectedWallet = WalletDto(defaultWalletKeys.ecKey)

        given(cryptoService.getMasterKey(seedPhrase)).willReturn(masterKeys)
        given(cryptoService.getDerivationKey(masterKeys, derivationPath)).willReturn(defaultWalletKeys)

        val result = webClient.post().uri("/rpc/accounts/doDerive")
            .body(Mono.just(derivationKeyRequest), DerivationKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(result).isEqualTo(ObjectMapper().writeValueAsString(expectedWallet))
    }

    @Test
    fun getBalanceShouldReturnWalletBalanceTest() {
        val address = "address"
        val expectedBalance = 1L

        given(walletService.getBalanceByAddress(address)).willReturn(expectedBalance)
        given(nodeClock.networkTime()).willReturn(0)
        given(nodeProperties.version).willReturn("1")

        val actualResult = webClient.get().uri("/rpc/accounts/wallets/$address/balance")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!

        assertThat(actualResult).isEqualTo(ObjectMapper().writeValueAsString(expectedBalance))
    }

}
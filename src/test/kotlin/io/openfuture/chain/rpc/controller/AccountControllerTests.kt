package io.openfuture.chain.rpc.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.rpc.domain.ResponseHeader
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import io.openfuture.chain.rpc.domain.crypto.key.RestoreRequest
import io.openfuture.chain.service.CryptoService
import io.openfuture.chain.service.WalletService
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
        val accountDto = createAccountDto()
        val expectedAccount = AccountDto("1 2 3 4 5 6 7 8 9 10 11 12", accountDto.keys, accountDto)
        val expectedBody = getRestResponse(expectedAccount)

        given(cryptoService.getRootAccount(expectedAccount.seedPhrase)).willReturn(expectedAccount)

        val result = webClient.post().uri("/rpc/accounts/doRestore")
            .body(Mono.just(RestoreRequest(expectedAccount.seedPhrase)), RestoreRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(RestResponse::class.java)
            .returnResult().responseBody!!

        assertThat(ObjectMapper().writeValueAsString(result.body))
            .isEqualTo(ObjectMapper().writeValueAsString(expectedBody.body))
    }

    @Test
    fun doDeriveReturnDerivationKeyWhenSeedPhraseDerivationPathAndSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val masterKey = ExtendedKey.root(ByteArray(32))
        val derivationKeyRequest = DerivationKeyRequest(seedPhrase, derivationPath)
        val expectedBody = getRestResponse(createAccountDto())

        given(cryptoService.serializePublicKey(any(ExtendedKey::class.java))).willReturn("1")
        given(cryptoService.serializePrivateKey(any(ExtendedKey::class.java))).willReturn("2")
        given(cryptoService.getDerivationKey(seedPhrase, derivationPath)).willReturn(masterKey)

        val result = webClient.post().uri("/rpc/accounts/doDerive")
            .body(Mono.just(derivationKeyRequest), DerivationKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(RestResponse::class.java)
            .returnResult().responseBody!!

        assertThat(ObjectMapper().writeValueAsString(result.body))
            .isEqualTo(ObjectMapper().writeValueAsString(expectedBody.body))
    }

    @Test
    fun getBalanceShouldReturnWalletBalanceTest() {
        val address = "address"
        val expectedBalance = 1L
        val expectedResponse = RestResponse(ResponseHeader(0, "1"), expectedBalance)

        given(walletService.getBalanceByAddress(address)).willReturn(expectedBalance)
        given(nodeClock.networkTime()).willReturn(0)
        given(nodeProperties.version).willReturn("1")

        val actualResult = webClient.get().uri("/rpc/accounts/wallets/$address/balance")
            .exchange()
            .expectStatus().isOk
            .expectBody(RestResponse::class.java)
            .returnResult().responseBody!!

        assertThat(ObjectMapper().writeValueAsString(actualResult.body))
            .isEqualTo(ObjectMapper().writeValueAsString(expectedResponse.body))
    }

    private fun createAccountDto(): WalletDto =
        WalletDto(KeyDto("1", "2"), "0x83a1e77Bd25daADd7A889BC36AC207A7D39CFD02")

    private fun <T> getRestResponse(body: T): RestResponse<T> {
        given(nodeClock.networkTime()).willReturn(0)
        given(nodeProperties.version).willReturn("0")

        return RestResponse(ResponseHeader(0, "0"), body)
    }

}
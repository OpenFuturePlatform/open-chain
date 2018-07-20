package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.rpc.crypto.WalletDto
import io.openfuture.chain.domain.rpc.crypto.AccountDto
import io.openfuture.chain.domain.rpc.crypto.key.DerivationKeyRequest
import io.openfuture.chain.domain.rpc.crypto.key.KeyDto
import io.openfuture.chain.domain.rpc.crypto.key.RestoreRequest
import io.openfuture.chain.service.CryptoService
import io.openfuture.chain.service.WalletService
import org.assertj.core.api.Assertions
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
    private lateinit var walletService: WalletService


    @Test
    fun doRestoreShouldReturnRootAccountInfoWhenSeedPhraseSent() {
        val accountDto = createAccountDto()
        val expectedAccount = AccountDto("1 2 3 4 5 6 7 8 9 10 11 12", accountDto.keys, accountDto)

        given(cryptoService.getRootAccount(expectedAccount.seedPhrase)).willReturn(expectedAccount)

        webClient.post().uri("${PathConstant.RPC}/accounts/doRestore")
            .body(Mono.just(RestoreRequest(expectedAccount.seedPhrase)), RestoreRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(AccountDto::class.java).isEqualTo<Nothing>(expectedAccount)
    }

    @Test
    fun doDeriveReturnDerivationKeyWhenSeedPhraseDerivationPathAndSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val masterKey = ExtendedKey.root(ByteArray(32))
        val derivationKeyRequest = DerivationKeyRequest(seedPhrase, derivationPath)
        val expectedAccount = createAccountDto()

        given(cryptoService.serializePublicKey(any(ExtendedKey::class.java))).willReturn("1")
        given(cryptoService.serializePrivateKey(any(ExtendedKey::class.java))).willReturn("2")
        given(cryptoService.getDerivationKey(seedPhrase, derivationPath)).willReturn(masterKey)

        webClient.post().uri("${PathConstant.RPC}/accounts/doDerive")
            .body(Mono.just(derivationKeyRequest), DerivationKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(WalletDto::class.java).isEqualTo<Nothing>(expectedAccount)
    }

    @Test
    fun getBalanceShouldReturnWalletBalanceTest() {
        val address = "address"
        val expectedBalance = 1L

        given(walletService.getBalance(address)).willReturn(expectedBalance)

        val actualResult = webClient.get().uri("${PathConstant.RPC}/accounts/wallets/$address/balance")
            .exchange()
            .expectStatus().isOk
            .expectBody(Long::class.java)
            .returnResult().responseBody!!

        Assertions.assertThat(actualResult).isEqualTo(expectedBalance)
    }

    private fun createAccountDto(): WalletDto =
        WalletDto(KeyDto("1", "2"), "0x83a1e77Bd25daADd7A889BC36AC207A7D39CFD02")

}
package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.crypto.AccountDto
import io.openfuture.chain.domain.crypto.RootAccountDto
import io.openfuture.chain.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.domain.crypto.key.RestoreRequest
import io.openfuture.chain.service.CryptoService
import org.junit.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

@WebFluxTest(CryptoController::class)
class CryptoControllerTests : ControllerTests() {

    @MockBean
    private lateinit var cryptoService: CryptoService


    @Test
    fun doRestoreShouldReturnRootAccountInfoWhenSeedPhraseSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val restoreRequest = RestoreRequest(seedPhrase)
        val expectedAccount = RootAccountDto(
            seedPhrase,
            "1",
            "2",
            AccountDto("1", "2", "0x83a1e77bd25daadd7a889bc36ac207a7d39cfd02")
        )

        given(cryptoService.getRootAccount(seedPhrase)).willReturn(expectedAccount)

        webClient.post().uri("${PathConstant.RPC}/crypto/doRestore")
            .body(Mono.just(restoreRequest), RestoreRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(RootAccountDto::class.java).isEqualTo<Nothing>(expectedAccount)
    }

    @Test
    fun doDeriveReturnDerivationKeyWhenSeedPhraseDerivationPathAndSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val seed = ByteArray(32)
        val masterKey = ExtendedKey.root(seed)
        val derivationKeyRequest = DerivationKeyRequest(seedPhrase, derivationPath)
        val expectedAccount = AccountDto("1", "2", "0x83a1e77bd25daadd7a889bc36ac207a7d39cfd02")

        given(cryptoService.serializePublicKey(any(ExtendedKey::class.java))).willReturn("1")
        given(cryptoService.serializePrivateKey(any(ExtendedKey::class.java))).willReturn("2")
        given(cryptoService.getDerivationKey(seedPhrase, derivationPath)).willReturn(masterKey)

        webClient.post().uri("${PathConstant.RPC}/crypto/doDerive")
            .body(Mono.just(derivationKeyRequest), DerivationKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(AccountDto::class.java).isEqualTo<Nothing>(expectedAccount)
    }

}
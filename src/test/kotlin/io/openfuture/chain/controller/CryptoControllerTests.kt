package io.openfuture.chain.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.domain.crypto.DerivationKeyRequest
import io.openfuture.chain.domain.crypto.MasterKeyRequest
import io.openfuture.chain.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.domain.crypto.key.AddressKeyDto
import io.openfuture.chain.domain.crypto.key.KeyDto
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
    fun doGenerateMasterReturnMasterKeyWhenSeedPhraseSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val seed = ByteArray(32)
        val masterKey = ExtendedKey.root(seed)
        val masterKeyRequest = MasterKeyRequest(seedPhrase)
        val expectedAddress = AddressKeyDto("1", "2")

        given(cryptoService.serializePublicKey(any(ExtendedKey::class.java))).willReturn("1")
        given(cryptoService.serializePrivateKey(any(ExtendedKey::class.java))).willReturn("2")
        given(cryptoService.getMasterKey(seedPhrase)).willReturn(masterKey)

        webClient.post().uri("${PathConstant.RPC}/crypto/doGenerateMaster")
            .body(Mono.just(masterKeyRequest), MasterKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(AddressKeyDto::class.java).isEqualTo<Nothing>(expectedAddress)
    }

    @Test
    fun doDeriveReturnDerivationKeyWhenSeedPhraseDerivationPathAndSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val seed = ByteArray(32)
        val masterKey = ExtendedKey.root(seed)
        val derivationKeyRequest = DerivationKeyRequest(seedPhrase, derivationPath)
        val expectedKey = KeyDto("1", "2")

        given(cryptoService.serializePublicKey(any(ExtendedKey::class.java))).willReturn("1")
        given(cryptoService.serializePrivateKey(any(ExtendedKey::class.java))).willReturn("2")
        given(cryptoService.getDerivationKey(seedPhrase, derivationPath)).willReturn(masterKey)

        webClient.post().uri("${PathConstant.RPC}/crypto/doDerive")
            .body(Mono.just(derivationKeyRequest), DerivationKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(KeyDto::class.java).isEqualTo<Nothing>(expectedKey)
    }

    @Test
    fun validateAddressShouldReturnAddressAndStatusOk() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36e"
        val request = ValidateAddressRequest(address)
        
        webClient.post().uri("${PathConstant.RPC}/crypto/validateAddress")
            .body(Mono.just(request), ValidateAddressRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(ValidateAddressRequest::class.java).isEqualTo<Nothing>(request)
    }

    @Test
    fun validateAddressShouldReturnStatusBadRequest() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36eaaaa"
        val request = ValidateAddressRequest(address)

        webClient.post().uri("${PathConstant.RPC}/crypto/validateAddress")
            .body(Mono.just(request), ValidateAddressRequest::class.java)
            .exchange()
            .expectStatus().isBadRequest
    }

}
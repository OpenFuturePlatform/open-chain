package io.openfuture.chain.rpc.controller

import io.openfuture.chain.config.ControllerTests
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.rpc.domain.crypto.AccountDto
import io.openfuture.chain.rpc.domain.crypto.ValidateAddressRequest
import io.openfuture.chain.rpc.domain.crypto.WalletDto
import io.openfuture.chain.rpc.domain.crypto.key.DerivationKeyRequest
import io.openfuture.chain.rpc.domain.crypto.key.ImportKeyRequest
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
    private lateinit var stateManager: StateManager

    @MockBean
    private lateinit var transactionManager: TransactionManager

    companion object {
        private const val ACCOUNT_URL = "/rpc/accounts"
    }


    @Test
    fun validateAddressShouldReturnAddressAndStatusOk() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36e"
        val request = ValidateAddressRequest(address)

        webClient.post().uri("$ACCOUNT_URL/wallets/validateAddress")
            .body(Mono.just(request), ValidateAddressRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(ValidateAddressRequest::class.java).isEqualTo<Nothing>(request)
    }

    @Test
    fun validateAddressShouldReturnStatusBadRequest() {
        val address = "0x5aF3B0FFB89C09D7A38Fd01E42E0A5e32011e36eaaaa"
        val request = ValidateAddressRequest(address)

        webClient.post().uri("$ACCOUNT_URL/wallets/validateAddress")
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

        val actualAccount = webClient.post().uri("$ACCOUNT_URL/doRestore")
            .body(Mono.just(RestoreRequest(expectedAccount.seedPhrase)), RestoreRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(AccountDto::class.java)
            .returnResult().responseBody!!

        assertThat(actualAccount).isEqualTo(expectedAccount)
    }

    @Test
    fun doDeriveReturnDerivationKeyWhenSeedPhraseDerivationPathAndSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val derivationKeyRequest = DerivationKeyRequest(seedPhrase, derivationPath)
        val masterKeys = ExtendedKey.root(ByteArray(0))
        val defaultWalletKeys = ExtendedKey.root(ByteArray(1))
        val expectedWalletDto = WalletDto(defaultWalletKeys.ecKey)

        given(cryptoService.getMasterKey(seedPhrase)).willReturn(masterKeys)
        given(cryptoService.getDerivationKey(masterKeys, derivationPath)).willReturn(defaultWalletKeys)

        val actualWalletDto = webClient.post().uri("$ACCOUNT_URL/doDerive")
            .body(Mono.just(derivationKeyRequest), DerivationKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(WalletDto::class.java)
            .returnResult().responseBody!!

        assertThat(actualWalletDto).isEqualTo(expectedWalletDto)
    }

    @Test
    fun getBalanceShouldReturnWalletBalance() {
        val address = "0x51c5311F25206De4A9C6ecAa1Bc2Be257B0bA1fb"
        val balance = 1L
        val unconfirmedBalance = 1L

        given(stateManager.getWalletBalanceByAddress(address)).willReturn(balance)
        given(transactionManager.getUnconfirmedBalanceBySenderAddress(address)).willReturn(unconfirmedBalance)

        val actualBalance = webClient.get().uri("$ACCOUNT_URL/wallets/$address/balance")
            .exchange()
            .expectStatus().isOk
            .expectBody(Long::class.java)
            .returnResult().responseBody!!

        assertThat(actualBalance).isEqualTo(balance - unconfirmedBalance)
    }

    @Test
    fun doGenerateNewAccountShouldReturnGeneratedAccount() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val masterKeys = ExtendedKey.root(ByteArray(0))
        val defaultDerivationKey = ExtendedKey.root(ByteArray(0))
        val expectedAccountDto = AccountDto(seedPhrase, KeyDto(masterKeys.ecKey), WalletDto(defaultDerivationKey.ecKey))

        given(cryptoService.generateSeedPhrase()).willReturn(seedPhrase)
        given(cryptoService.getMasterKey(seedPhrase)).willReturn(masterKeys)
        given(cryptoService.getDefaultDerivationKey(masterKeys)).willReturn(defaultDerivationKey)

        val actualAccountDto = webClient.get().uri("$ACCOUNT_URL/doGenerate")
            .exchange()
            .expectStatus().isOk
            .expectBody(AccountDto::class.java)
            .returnResult().responseBody!!

        assertThat(actualAccountDto).isEqualTo(expectedAccountDto)
    }

    @Test
    fun doImportKeyShouldReturnWalletDto() {
        val importKeyRequest = ImportKeyRequest("decodedKey")
        val key = ExtendedKey.root(ByteArray(0))
        val publicKey = "publicKey"
        val privateKey = "privateKey"
        val expectedWalletDto = WalletDto(KeyDto("publicKey", "privateKey"), key.ecKey.getAddress())

        given(cryptoService.importExtendedKey(importKeyRequest.decodedKey!!)).willReturn(key)
        given(cryptoService.serializePublicKey(key)).willReturn(publicKey)
        given(cryptoService.serializePrivateKey(key)).willReturn(privateKey)


        val actualWalletDto = webClient.post().uri("$ACCOUNT_URL/keys/doExtendedImport")
            .body(Mono.just(importKeyRequest), ImportKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(WalletDto::class.java)
            .returnResult().responseBody!!

        assertThat(actualWalletDto).isEqualTo(expectedWalletDto)
    }

    @Test
    fun doImportWifKeyShouldReturnWalletDto() {
        val importKeyRequest = ImportKeyRequest("decodedKey")
        val ecKey = ECKey(ByteArray(1))
        val expectedWalletDto = WalletDto(ecKey)

        given(cryptoService.importWifKey(importKeyRequest.decodedKey!!)).willReturn(ecKey)

        val actualWalletDto = webClient.post().uri("$ACCOUNT_URL/keys/doWifImport")
            .body(Mono.just(importKeyRequest), ImportKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(WalletDto::class.java)
            .returnResult().responseBody!!

        assertThat(actualWalletDto).isEqualTo(expectedWalletDto)
    }

    @Test
    fun doImportPrivateKeyShouldReturnWalletDto() {
        val importKeyRequest = ImportKeyRequest("decodedKey")
        val ecKey = ECKey(ByteArray(1))
        val expectedWalletDto = WalletDto(ecKey)

        given(cryptoService.importPrivateKey(importKeyRequest.decodedKey!!)).willReturn(ecKey)

        val actualWalletDto = webClient.post().uri("$ACCOUNT_URL/keys/doPrivateImport")
            .body(Mono.just(importKeyRequest), ImportKeyRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(WalletDto::class.java)
            .returnResult().responseBody!!

        assertThat(actualWalletDto).isEqualTo(expectedWalletDto)
    }

}
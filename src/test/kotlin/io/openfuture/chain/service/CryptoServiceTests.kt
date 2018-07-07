package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.key.PrivateKeyManager
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.crypto.seed.validator.SeedPhraseValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class CryptoServiceTests : ServiceTests() {

    @Mock private lateinit var seedPhraseGenerator: SeedPhraseGenerator
    @Mock private lateinit var seedCalculator: SeedCalculator
    @Mock private lateinit var derivationKeysHelper: DerivationKeysHelper
    @Mock private lateinit var serializer: ExtendedKeySerializer
    @Mock private lateinit var seedPhraseValidator: SeedPhraseValidator
    @Mock private lateinit var keyManager: PrivateKeyManager
    @Mock private lateinit var deserializer: ExtendedKeyDeserializer

    private lateinit var cryptoService: DefaultCryptoService


    @Before
    fun setUp() {
        cryptoService = DefaultCryptoService(
            seedPhraseGenerator,
            keyManager,
            serializer,
            deserializer,
            derivationKeysHelper,
            seedPhraseValidator,
            seedCalculator
        )
    }

    @Test
    fun generateSeedPhraseShouldReturnTwelveSeedPhraseWords() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(seedPhraseGenerator.createSeedPhrase(TWELVE)).willReturn(seedPhrase)

        val seedPhraseResult = cryptoService.generateSeedPhrase()

        assertThat(seedPhrase).isEqualTo(seedPhraseResult)
    }

    @Test
    fun getRootAccountKeyShouldReturnRootInfoBySeedPhrase() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val seed = ByteArray(32)

        given(seedPhraseValidator.validate(seedPhrase)).willReturn(true)
        given(seedCalculator.calculateSeed(seedPhrase)).willReturn(seed)
        given(derivationKeysHelper.deriveDefaultAddress(any(ExtendedKey::class.java))).will { invocation -> invocation.arguments[0] }
        given(serializer.serializePublic(any(ExtendedKey::class.java))).willReturn("1")
        given(serializer.serializePrivate(any(ExtendedKey::class.java))).willReturn("2")

        val actualRootAccount = cryptoService.getRootAccount(seedPhrase)

        assertThat(actualRootAccount.seedPhrase).isEqualTo(seedPhrase)
        assertThat(actualRootAccount.masterPublicKey).isEqualTo("1")
        assertThat(actualRootAccount.masterPrivateKey).isEqualTo("2")
        assertThat(actualRootAccount.defaultAccount.publicKey).isEqualTo("1")
        assertThat(actualRootAccount.defaultAccount.privateKey).isEqualTo("2")
        assertThat(actualRootAccount.defaultAccount.address).isNotNull()
    }

    @Test(expected = IllegalArgumentException::class)
    fun getRootAccountKeyShouldThrowIllegalArgumentExceptionWhenInvalidSeedPhrase() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(seedPhraseValidator.validate(seedPhrase)).willReturn(false)

        cryptoService.getRootAccount(seedPhrase)
    }

    @Test
    fun getDerivationKeyShouldReturnDerivationKeyWhenSeedPhraseSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)

        given(seedPhraseValidator.validate(seedPhrase)).willReturn(true)
        given(seedCalculator.calculateSeed(seedPhrase)).willReturn(seed)
        given(derivationKeysHelper.derive(any(ExtendedKey::class.java), any(String::class.java))).willReturn(extendedKey)

        val key = cryptoService.getDerivationKey(seedPhrase, derivationPath)

        assertExtendedKey(key)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getDerivationKeyShouldThrowIllegalArgumentExceptionWhenInvalidSeedPhrase() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"

        given(seedPhraseValidator.validate(seedPhrase)).willReturn(false)

        cryptoService.getDerivationKey(seedPhrase, derivationPath)
    }

    @Test
    fun serializedPublicKeyShouldReturnSerializedPublicKey() {
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)
        val expectedPublicKey = "123456678890"

        given(serializer.serializePublic(extendedKey)).willReturn(expectedPublicKey)

        val publicKey = cryptoService.serializePublicKey(extendedKey)

        assertThat(publicKey).isEqualTo(expectedPublicKey)
    }

    @Test
    fun serializedPrivateKeyShouldReturnSerializedPublicKey() {
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)
        val expectedPrivateKey = "123456678890"

        given(serializer.serializePrivate(extendedKey)).willReturn(expectedPrivateKey)

        val privateKey = cryptoService.serializePrivateKey(extendedKey)

        assertThat(privateKey).isEqualTo(expectedPrivateKey)
    }

    fun generateNewAccountShouldReturnRootAccountInfo() {
        val seedPhrase = "ability able about above absent absorb abstract absurd abuse accident access act"
        val privateKey = "xprv9s21ZrQH143K4QKw9Cq9BUSUJGMSNMBt5mQVU8QD32NZpw4i6bnmiACNkqunc6P6B5tHXGw4oJMo2wXVwDgj2WDQFpTFufd4TdtKpZvpgEb"
        val publicKey = "xpub661MyMwAqRbcGtQQFEN9YcPCrJBvmoujSzL6GWopbMuYhjPre972FxWrc6NHZiH87hAz3vg3o95GDTwncHF6dMkoJLQ897p4VssRDA4kJ7V"

        given(seedPhraseGenerator.createSeedPhrase(TWELVE)).willReturn(seedPhrase)
        given(derivationKeysHelper.deriveDefaultAddress(any(ExtendedKey::class.java))).will { invocation -> invocation.arguments[0] }
        given(serializer.serializePublic(any(ExtendedKey::class.java))).willReturn(publicKey)
        given(serializer.serializePrivate(any(ExtendedKey::class.java))).willReturn(privateKey)

        val actualRootAccount = cryptoService.generateNewAccount()

        assertThat(actualRootAccount.seedPhrase).isEqualTo(seedPhrase)
        assertThat(actualRootAccount.masterPublicKey).isEqualTo(publicKey)
        assertThat(actualRootAccount.masterPrivateKey).isEqualTo(privateKey)
        assertThat(actualRootAccount.defaultAccount.publicKey).isEqualTo(publicKey)
        assertThat(actualRootAccount.defaultAccount.privateKey).isEqualTo(privateKey)
        assertThat(actualRootAccount.defaultAccount.address).isNotNull()
    }

    fun importKeyShouldReturnKeysValuesAndAddressWhenPrivateKeyImporting() {
        val decodedKey = "xpub661MyMwAqRbcF1xAwgn4pRrb25d3iSwvBC4DaTsNSUcoLZ6y4jgG2gtTGNjSVSvLzLMEawq1ghm1XkJ2QEzU3"
        val extendedKey = ExtendedKey(ByteArray(64))

        given(deserializer.deserialize(decodedKey)).willReturn(extendedKey)

        val importedKey = cryptoService.importKey(decodedKey)

        assertThat(importedKey.ecKey.public).isNotNull()
        assertThat(importedKey.ecKey.private).isNotNull()
        assertThat(importedKey.ecKey.getAddress()).isNotBlank()
    }

    @Test
    fun importKeyShouldReturnPublicKeyValueAndAddressWhenPublicKeyImporting() {
        val decodedKey = "xpub661MyMwAqRbcF1xAwgn4pRrb25d3iSwvBC4DaTsNSUcoLZ6y4jgG2gtTGNjSVSvLzLMEawq1ghm1XkJ2QEzU3"
        val extendedKey = ExtendedKey(ByteArray(64), ecKey = ECKey(ByteArray(0), false))

        given(deserializer.deserialize(decodedKey)).willReturn(extendedKey)

        val importedKey = cryptoService.importKey(decodedKey)

        assertThat(importedKey.ecKey.public).isNotNull()
        assertThat(importedKey.ecKey.getAddress()).isNotNull()
        assertThat(importedKey.ecKey.private).isNull()

    }

    @Test
    fun importWifKeyShouldReturnKeysValuesAndAddress() {
        val wifKey = "Kz5FUmSQf37sncxHS9LRGaUGokh9syGhwdZEFdYNX5y9uVZH8myo"
        val ecKey = ECKey(ByteArray(0))

        given(keyManager.importPrivateKey(wifKey)).willReturn(ecKey)

        val importedKey = cryptoService.importWifKey(wifKey)

        assertThat(importedKey.public).isNotNull()
        assertThat(importedKey.private).isNotNull()
        assertThat(importedKey.getAddress()).isNotBlank()
    }

    private fun assertExtendedKey(key: ExtendedKey) {
        assertThat(key).isNotNull
        assertThat(key.chainCode).isNotNull()
        assertThat(key.depth).isNotNull()
        assertThat(key.ecKey).isNotNull
        assertThat(key.ecKey.public).isNotNull()
        assertThat(key.ecKey.private).isNotNull()
        assertThat(key.parentFingerprint).isNotNull()
        assertThat(key.sequence).isNotNull()
    }

}

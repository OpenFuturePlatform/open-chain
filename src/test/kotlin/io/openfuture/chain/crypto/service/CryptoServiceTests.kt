package io.openfuture.chain.crypto.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.component.key.DerivationKeysHelper
import io.openfuture.chain.crypto.component.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.component.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.component.key.PrivateKeyManager
import io.openfuture.chain.crypto.component.seed.SeedCalculator
import io.openfuture.chain.crypto.component.seed.SeedPhraseGenerator
import io.openfuture.chain.crypto.model.dictionary.PhraseLength.TWELVE
import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.model.dto.ExtendedKey
import io.openfuture.chain.crypto.validation.SeedPhraseValidator
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
    fun getDerivationKeyShouldReturnDerivationKeyWhenSeedPhraseSent() {
        val derivationPath = "m/0"
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)

        given(derivationKeysHelper.derive(any(ExtendedKey::class.java), any(String::class.java))).willReturn(extendedKey)

        val key = cryptoService.getDerivationKey(extendedKey, derivationPath)

        assertExtendedKey(key)
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

    @Test
    fun getMasterKeysShouldReturnMasterKeysPair() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val seed = ByteArray(32)

        given(seedPhraseValidator.isValid(seedPhrase)).willReturn(true)
        given(seedCalculator.calculateSeed(seedPhrase)).willReturn(seed)

        val key = cryptoService.getMasterKey(seedPhrase)

        assertExtendedKey(key)
    }

    @Test
    fun isValidAddressShouldReturnTrue() {
        val address = "0x290DEcD9548b62A8D60345A988386Fc84Ba6BC95"

        val result = cryptoService.isValidAddress(address, ByteArray(32))

        assertThat(result).isTrue()
    }

    @Test
    fun isValidAddressShouldReturnFalse() {
        val address = "0x290DEcD9548b62A8D60345A988386Fc84Ba6BC956"

        val result = cryptoService.isValidAddress(address, ByteArray(32))

        assertThat(result).isFalse()
    }

    @Test
    fun importExtendedKeyShouldReturnKeysValuesAndAddressWhenPrivateKeyImporting() {
        val decodedKey = "xpub661MyMwAqRbcF1xAwgn4pRrb25d3iSwvBC4DaTsNSUcoLZ6y4jgG2gtTGNjSVSvLzLMEawq1ghm1XkJ2QEzU3"
        val extendedKey = ExtendedKey(ByteArray(64))

        given(deserializer.deserialize(decodedKey)).willReturn(extendedKey)

        val importedKey = cryptoService.importExtendedKey(decodedKey)

        assertThat(importedKey.ecKey.public).isNotNull()
        assertThat(importedKey.ecKey.private).isNotNull()
        assertThat(importedKey.ecKey.getAddress()).isNotBlank()
    }

    @Test
    fun importExtendedKeyShouldReturnPublicKeyValueAndAddressWhenPublicKeyImporting() {
        val decodedKey = "xpub661MyMwAqRbcF1xAwgn4pRrb25d3iSwvBC4DaTsNSUcoLZ6y4jgG2gtTGNjSVSvLzLMEawq1ghm1XkJ2QEzU3"
        val extendedKey = ExtendedKey(ByteArray(64), ecKey = ECKey(ByteArray(0), false))

        given(deserializer.deserialize(decodedKey)).willReturn(extendedKey)

        val importedKey = cryptoService.importExtendedKey(decodedKey)

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

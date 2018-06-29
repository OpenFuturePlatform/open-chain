package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength
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

    @Mock private lateinit var derivationKeyHelper: DerivationKeysHelper

    @Mock private lateinit var extendedKeySerializer: ExtendedKeySerializer

    @Mock private lateinit var seedPhraseValidator: SeedPhraseValidator

    private lateinit var cryptoService: DefaultCryptoService


    @Before
    fun setUp() {
        cryptoService = DefaultCryptoService(
                seedPhraseGenerator,
                seedCalculator,
                derivationKeyHelper,
                extendedKeySerializer,
                seedPhraseValidator
        )
    }

    @Test
    fun generateSeedPhraseShouldReturnTwelveSeedPhraseWords() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)).willReturn(seedPhrase)

        val seedPhraseResult = cryptoService.generateSeedPhrase()

        assertThat(seedPhrase).isEqualTo(seedPhraseResult)
    }

    @Test
    fun getMasterKeyShouldReturnMasterKeyWhenSeedPhraseSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val seed = ByteArray(32)

        given(seedCalculator.calculateSeed(seedPhrase)).willReturn(seed)

        val key = cryptoService.getMasterKey(seedPhrase)

        assertThat(key).isNotNull
    }

    @Test
    fun getDerivationKeyShouldReturnDerivationKeyWhenSeedPhraseSent() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"
        val derivationPath = "m/0"
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)

        given(seedCalculator.calculateSeed(seedPhrase)).willReturn(seed)
        given(derivationKeyHelper.derive(any(ExtendedKey::class.java), any(String::class.java))).willReturn(extendedKey)

        val key = cryptoService.getDerivationKey(seedPhrase, derivationPath)

        assertExtendedKey(key)
    }

    @Test
    fun serializedPublicKeyShouldReturnSerializedPublicKey()  {
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)
        val expectedPublicKey = "123456678890"

        given(extendedKeySerializer.serializePublic(extendedKey)).willReturn(expectedPublicKey)

        val publicKey = cryptoService.serializedPublicKey(extendedKey)
        assertThat(expectedPublicKey).isEqualTo(publicKey)
    }

    @Test
    fun serializedPrivateKeyShouldReturnSerializedPublicKey()  {
        val seed = ByteArray(32)
        val extendedKey = ExtendedKey.root(seed)
        val expectedPrivateKey = "123456678890"

        given(extendedKeySerializer.serializePrivate(extendedKey)).willReturn(expectedPrivateKey)

        val privateKey = cryptoService.serializedPrivateKey(extendedKey)
        assertThat(expectedPrivateKey).isEqualTo(privateKey)
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

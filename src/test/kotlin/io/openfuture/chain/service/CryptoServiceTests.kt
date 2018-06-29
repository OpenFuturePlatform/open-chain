package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength
import io.openfuture.chain.crypto.seed.calculator.SeedCalculator
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.crypto.seed.validator.SeedPhraseValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class CryptoServiceTests : ServiceTests() {

    @Mock private lateinit var seedPhraseGenerator: SeedPhraseGenerator

    @Mock private lateinit var derivationKeysHelper: DerivationKeysHelper

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

        given(seedPhraseGenerator.createSeedPhrase(TWELVE)).willReturn(seedPhrase)

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
    fun generateKeyShouldReturnWalletDtoTest() {
        val seedPhrase = "ability able about above absent absorb abstract absurd abuse accident access act"
        val privateKey  = "xprv9s21ZrQH143K4QKw9Cq9BUSUJGMSNMBt5mQVU8QD32NZpw4i6bnmiACNkqunc6P6B5tHXGw4oJMo2wXVwDgj2WDQFpTFufd4TdtKpZvpgEb"
        val publicKey = "xpub661MyMwAqRbcGtQQFEN9YcPCrJBvmoujSzL6GWopbMuYhjPre972FxWrc6NHZiH87hAz3vg3o95GDTwncHF6dMkoJLQ897p4VssRDA4kJ7V"

        given(seedPhraseGenerator.createSeedPhrase(TWELVE)).willReturn(seedPhrase)
        given(derivationKeysHelper.deriveDefaultAddress(any(ExtendedKey::class.java))).will { invocation -> invocation.arguments[0]  }
        given(extendedKeySerializer.serializePublic(any(ExtendedKey::class.java))).willReturn(publicKey)
        given(extendedKeySerializer.serializePrivate(any(ExtendedKey::class.java))).willReturn(privateKey)

        val actualWalletDto = cryptoService.generateKey()

        assertThat(actualWalletDto.seedPhrase).isEqualTo(seedPhrase)
        assertThat(actualWalletDto.masterPublicKey).isEqualTo(publicKey)
        assertThat(actualWalletDto.masterPrivateKey).isEqualTo(privateKey)
        assertThat(actualWalletDto.addressKeyDto.publicKey).isEqualTo(publicKey)
        assertThat(actualWalletDto.addressKeyDto.privateKey).isEqualTo(privateKey)
        assertThat(actualWalletDto.addressKeyDto.address).isNotNull()
    }

}

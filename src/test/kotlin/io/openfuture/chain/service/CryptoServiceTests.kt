package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.DerivationKeysHelper
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.seed.PhraseLength.TWELVE
import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.domain.ExtendedKey
import io.openfuture.chain.crypto.key.ExtendedKeyDeserializer
import io.openfuture.chain.crypto.key.ExtendedKeySerializer
import io.openfuture.chain.crypto.key.PrivateKeyManager
import io.openfuture.chain.crypto.seed.PhraseLength
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

class CryptoServiceTests : ServiceTests() {

    @Mock private lateinit var seedPhraseGenerator: SeedPhraseGenerator
    @Mock private lateinit var extendedKeySerializer: ExtendedKeySerializer
    @Mock private lateinit var derivationKeysHelper: DerivationKeysHelper
    @Mock private lateinit var keyManager: PrivateKeyManager
    @Mock private lateinit var serializer: ExtendedKeySerializer
    @Mock private lateinit var deserializer: ExtendedKeyDeserializer

    @InjectMocks
    private lateinit var cryptoService: DefaultCryptoService


    @Test
    fun generateBip44SeedPhraseShouldReturnTwelveSeedPhraseWords() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(seedPhraseGenerator.createSeedPhrase(TWELVE)).willReturn(seedPhrase)

        val seedPhraseResult = cryptoService.generateSeedPhrase()

        assertThat(seedPhrase).isEqualTo(seedPhraseResult)
    }

    @Test
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

    @Test
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

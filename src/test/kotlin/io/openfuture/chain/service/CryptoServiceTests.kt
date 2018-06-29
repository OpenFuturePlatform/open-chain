package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
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

    @Mock private lateinit var keyManager: PrivateKeyManager

    @Mock private lateinit var serializer: ExtendedKeySerializer

    @Mock private lateinit var deserializer: ExtendedKeyDeserializer

    @InjectMocks
    private lateinit var cryptoService: DefaultCryptoService


    @Test
    fun generateBip44SeedPhraseShouldReturnTwelveSeedPhraseWords() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)).willReturn(seedPhrase)

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

}

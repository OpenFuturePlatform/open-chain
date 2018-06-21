package io.openfuture.chain.service

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.bip39.Bip39MnemonicGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

class SeedPhraseGeneratorServiceTests : ServiceTests() {

    @Mock
    private lateinit var bip39MnemonicGenerator: Bip39MnemonicGenerator

    @InjectMocks
    private lateinit var seedPhraseGeneratorService: DefaultSeedPhraseGeneratorService

    @Test
    fun generateBip44SeedPhraseShouldReturnTwelveSeedPhraseWords() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(bip39MnemonicGenerator.createMnemonic(any(ByteArray::class.java))).willReturn(seedPhrase)

        val seedPhraseResult = seedPhraseGeneratorService.generateBip44SeedPhrase()

        assertThat(seedPhrase).isEqualTo(seedPhraseResult)
    }

}

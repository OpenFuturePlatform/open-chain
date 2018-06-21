package io.openfuture.chain.crypto.bip44

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.crypto.seed.generator.dictionary.WordList
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

class SeedPhraseGeneratorTests : ServiceTests() {

    @Mock private lateinit var wordList: WordList

    @InjectMocks
    private lateinit var seedPhraseGenerator: SeedPhraseGenerator

    @Test
    fun createSeedPhraseWhenByteArrayIsTwelveBytesShouldReturnSeedPhrase() {
        var entropy = ByteArray(16)
        var word = "1"
        var expectedPhrase = "1 1 1 1 1 1 1 1 1 1 1 1"

        given(wordList.getSeparator()).willReturn(' ')
        given(wordList.getWord(any(Int::class.java))).willReturn(word)

        var seedPhraseResult = seedPhraseGenerator.createSeedPhrase(entropy)

        assertThat(seedPhraseResult).isEqualTo(expectedPhrase)
    }
}

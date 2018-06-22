package io.openfuture.chain.service

import io.openfuture.chain.component.seed.PhraseLength
import io.openfuture.chain.component.seed.generator.SeedPhraseGenerator
import io.openfuture.chain.config.ServiceTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

class SeedPhraseGeneratorServiceTests : ServiceTests() {

    @Mock
    private lateinit var seedPhraseGenerator: SeedPhraseGenerator

    @InjectMocks
    private lateinit var seedPhraseGeneratorService: DefaultSeedPhraseGeneratorService

    @Test
    fun generateBip44SeedPhraseShouldReturnTwelveSeedPhraseWords() {
        val seedPhrase = "1 2 3 4 5 6 7 8 9 10 11 12"

        given(seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)).willReturn(seedPhrase)

        val seedPhraseResult = seedPhraseGeneratorService.generateSeedPhrase()

        assertThat(seedPhrase).isEqualTo(seedPhraseResult)
    }

}

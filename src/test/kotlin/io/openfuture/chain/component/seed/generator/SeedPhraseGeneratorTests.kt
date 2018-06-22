package io.openfuture.chain.component.seed.generator

import io.openfuture.chain.component.seed.PhraseLength
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.entity.SeedWord
import io.openfuture.chain.repository.SeedWordRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock

class SeedPhraseGeneratorTests : ServiceTests() {

    @Mock private lateinit var seedWordRepository: SeedWordRepository

    @InjectMocks
    private lateinit var seedPhraseGenerator: SeedPhraseGenerator

    @Test
    fun createSeedPhraseWhenByteArrayIsTwelveBytesShouldReturnSeedPhrase() {
        val word = "1"
        val seedWord = SeedWord(0, word)
        val expectedPhrase = "$word $word $word $word $word $word $word $word $word $word $word $word"

        given(seedWordRepository.findOneByIndex(any(Int::class.java))).willReturn(seedWord)

        val seedPhraseResult = seedPhraseGenerator.createSeedPhrase(PhraseLength.TWELVE)

        assertThat(seedPhraseResult).isEqualTo(expectedPhrase)
    }
}

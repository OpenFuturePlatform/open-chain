package io.openfuture.chain.crypto.component.seed

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.crypto.model.dictionary.PhraseLength
import io.openfuture.chain.crypto.model.entity.SeedWord
import io.openfuture.chain.crypto.repository.SeedWordRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class SeedPhraseGeneratorTests : ServiceTests() {

    @Mock private lateinit var seedWordRepository: SeedWordRepository

    private lateinit var seedPhraseGenerator: SeedPhraseGenerator


    @Before
    fun setUp() {
        seedPhraseGenerator = SeedPhraseGenerator(seedWordRepository)
    }

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

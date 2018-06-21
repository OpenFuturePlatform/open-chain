package io.openfuture.chain.crypto.seed.generator.dictionary

import io.openfuture.chain.property.SeedProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class WordListTests {

    @Mock private lateinit var seedProperties: SeedProperties

    private lateinit var wordList: WordList

    @Before
    fun setUp() {
        given(seedProperties.dictionaryPath).willReturn("dictionary/seed_phrase_dictionary.txt")
        wordList = WordList(seedProperties)
    }

    @Test
    fun getWordWhenRequestWordWithIndex2047ShouldReturnNotBlankValue() {
        val wordResult = wordList.getWord(2047)

        assertThat(wordResult).isNotBlank()
    }

}

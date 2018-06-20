package io.openfuture.chain.crypto.bip39.dictionary

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
class WordListTests {

    @Autowired
    private lateinit var wordList: WordList

    @Test
    fun getWordWhenRequestWordWithIndex2047ShouldReturnNotBlankValue() {
        val wordResult = wordList.getWord(2047)

        assertThat(wordResult).isNotBlank()
    }

}

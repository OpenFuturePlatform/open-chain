package io.openfuture.chain.component.seed.generator.dictionary

import io.openfuture.chain.property.SeedProperties
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Component
import java.io.File

@Component
class WordList(private val properties: SeedProperties) {

    private lateinit var words: Array<String>

    init {
        initWords()
    }

    fun getWord(index: Int): String {
        return words[index]
    }

    fun getSeparator(): Char {
        return ' '
    }

    private fun initWords() {
        val wordsPath = javaClass.classLoader.getResource(properties.dictionaryPath).file
        val wordsFile = File(wordsPath)
        val wordsContent = FileUtils.readFileToString(wordsFile)
        words = wordsContent.split("\n").toTypedArray()
    }

}

package io.openfuture.chain.crypto.seed

enum class PhraseLength(val phraseLength: Int, val entropyLength: Int, val checkSumLength: Int) {

    TWELVE(12, 128, 4);

    companion object {
        fun fromPhraseLength(phraseLength: Int): PhraseLength? = values().singleOrNull { it.phraseLength == phraseLength }
    }

}
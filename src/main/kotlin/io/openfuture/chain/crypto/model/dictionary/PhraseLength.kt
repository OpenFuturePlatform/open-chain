package io.openfuture.chain.crypto.model.dictionary

enum class PhraseLength(val value: Int, val entropyLength: Int, val checkSumLength: Int) {

    TWELVE(12, 128, 4);

    companion object {
        fun fromValue(value: Int): PhraseLength? = values().singleOrNull { it.value == value }
    }

}
package io.openfuture.chain.crypto.seed

enum class PhraseLength(val phraseLength: Int, val entropyLength: Int, val checkSumLength: Int) {

    TWELVE(12, 128, 4);

    companion object {
        val phraseLengthMap = PhraseLength.values().associateBy(PhraseLength::phraseLength)

        fun fromPhaseLength(phraseLength: Int) = phraseLengthMap[phraseLength]
    }

}
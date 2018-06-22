package io.openfuture.chain.component.seed.generator

import io.openfuture.chain.component.seed.SeedGeneratorConstant
import io.openfuture.chain.repository.SeedWordRepository
import io.openfuture.chain.util.HashUtils
import io.openfuture.chain.util.Sha256Utils
import org.springframework.stereotype.Component
import java.util.*

@Component
class SeedPhraseGenerator(
        private val seedWordRepository: SeedWordRepository
) {

    fun createSeedPhrase(entropy: ByteArray): String {
        val target = StringBuilder()
        val wordIndexes = wordIndexes(entropy)
        try {
            createSeedPhrase(wordIndexes, target)
        } finally {
            Arrays.fill(wordIndexes, 0)
        }
        return target.toString()
    }

    private fun createSeedPhrase(wordIndexes: IntArray, target: StringBuilder) {
        for (i in wordIndexes.indices) {
            if (i > 0) {
                target.append(SeedGeneratorConstant.SEED_PHRASE_SEPARATOR)
            }
            val seedWord = seedWordRepository.findOneByWordIndex(wordIndexes[i])
            target.append(seedWord.wordValue)
        }
    }

    private fun wordIndexes(entropy: ByteArray): IntArray {
        val ent = entropy.size * SeedGeneratorConstant.BYTE_SIZE
        entropyLengthPreChecks(ent)

        val entropyWithChecksum = Arrays.copyOf(entropy, entropy.size + 1)
        entropyWithChecksum[entropy.size] = firstByteOfSha256(entropy)

        val checksumLength = ent / SeedGeneratorConstant.MULTIPLICITY_VALUE

        val mnemonicLength = (ent + checksumLength) / SeedGeneratorConstant.WORD_INDEX_SIZE

        val wordIndexes = IntArray(mnemonicLength)
        var bitOffset = 0
        var wordIndex = 0
        while (wordIndex < mnemonicLength) {
            wordIndexes[wordIndex] = HashUtils.nextWordsIndex(entropyWithChecksum, bitOffset)
            bitOffset += SeedGeneratorConstant.WORD_INDEX_SIZE
            wordIndex++
        }
        return wordIndexes
    }

    private fun firstByteOfSha256(entropy: ByteArray): Byte {
        val hash = Sha256Utils.sha256(entropy)
        val firstByte = hash[0]
        Arrays.fill(hash, 0.toByte())
        return firstByte
    }

    private fun entropyLengthPreChecks(ent: Int) {
        if (ent < SeedGeneratorConstant.MIN_ENTROPY_SIZE) {
            throw RuntimeException("Entropy too low, ${SeedGeneratorConstant.MIN_ENTROPY_SIZE}" +
                    "-${SeedGeneratorConstant.MAX_ENTROPY_SIZE} bits allowed")
        }
        if (ent > SeedGeneratorConstant.MAX_ENTROPY_SIZE) {
            throw RuntimeException("Entropy too low, ${SeedGeneratorConstant.MIN_ENTROPY_SIZE}" +
                    "-${SeedGeneratorConstant.MAX_ENTROPY_SIZE} bits allowed")
        }
        if (ent % SeedGeneratorConstant.MULTIPLICITY_VALUE > 0) {
            throw RuntimeException("Number of entropy bits must be divisible by ${SeedGeneratorConstant.MULTIPLICITY_VALUE}")
        }
    }

}

package io.openfuture.chain.component.seed.generator

import io.openfuture.chain.component.seed.PhraseLength
import io.openfuture.chain.component.seed.SeedGeneratorConstant
import io.openfuture.chain.repository.SeedWordRepository
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.security.SecureRandom
import java.util.*

@Component
class SeedPhraseGenerator(
        private val seedWordRepository: SeedWordRepository
) {

    companion object {
        const val SEED_PHRASE_SEPARATOR = " "
    }

    fun createSeedPhrase(length: PhraseLength): String {
        val entropy = ByteArray(length.getByteLength())
        SecureRandom().nextBytes(entropy)

        val wordIndexes = wordIndexes(entropy)
        val words = Array(wordIndexes.size) { seedWordRepository.findOneByIndex(wordIndexes[it]).value }
        return StringUtils.arrayToDelimitedString(words, SEED_PHRASE_SEPARATOR)
    }

    private fun wordIndexes(entropy: ByteArray): IntArray {
        val ent = entropy.size * SeedGeneratorConstant.BYTE_SIZE

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
        val hash = HashUtils.sha256(entropy)
        return hash[0]
    }

}

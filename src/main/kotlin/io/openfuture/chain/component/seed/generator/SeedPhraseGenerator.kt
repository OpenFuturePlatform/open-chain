package io.openfuture.chain.component.seed.generator

import io.openfuture.chain.component.seed.PhraseLength
import io.openfuture.chain.component.seed.SeedConstant
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
        private const val DOUBLE_BYTE_SIZE = 16
        private const val BYTE_MASK = 0xff
        private const val MAX_BYTES_TO_READ = 3
    }

    fun createSeedPhrase(length: PhraseLength): String {
        val entropy = ByteArray(length.getByteLength())
        SecureRandom().nextBytes(entropy)

        val wordIndexes = wordIndexes(entropy)
        val words = Array(wordIndexes.size) { seedWordRepository.findOneByIndex(wordIndexes[it]).value }
        return StringUtils.arrayToDelimitedString(words, SeedConstant.SEED_PHRASE_SEPARATOR)
    }

    private fun wordIndexes(entropy: ByteArray): IntArray {
        val entropySize = entropy.size * SeedConstant.BYTE_SIZE

        val entropyWithChecksum = Arrays.copyOf(entropy, entropy.size + 1)
        entropyWithChecksum[entropy.size] = HashUtils.sha256(entropy)[0]

        val checksumLength = entropySize / SeedConstant.MULTIPLICITY_VALUE
        val mnemonicLength = (entropySize + checksumLength) / SeedConstant.WORD_INDEX_SIZE

        val wordIndexes = IntArray(mnemonicLength)
        var bitOffset = 0
        var wordIndex = 0
        while (wordIndex < mnemonicLength) {
            wordIndexes[wordIndex] = nextWordsIndex(entropyWithChecksum, bitOffset)
            bitOffset += SeedConstant.WORD_INDEX_SIZE
            wordIndex++
        }
        return wordIndexes
    }

    private fun nextWordsIndex(bytes: ByteArray, offset: Int): Int {
        val skip = offset / SeedConstant.BYTE_SIZE
        val lowerBitsToRemove = (MAX_BYTES_TO_READ * SeedConstant.BYTE_SIZE - SeedConstant.WORD_INDEX_SIZE) -
                (offset % SeedConstant.BYTE_SIZE)

        val firstBytePart = bytes[skip].toInt() and BYTE_MASK shl DOUBLE_BYTE_SIZE
        val secondBytePart = bytes[skip + 1].toInt() and BYTE_MASK shl SeedConstant.BYTE_SIZE
        val thirdBytePart = if (lowerBitsToRemove < SeedConstant.BYTE_SIZE) {
            bytes[skip + 2].toInt() and BYTE_MASK
        } else {
            0
        }

        return ((firstBytePart or secondBytePart or thirdBytePart) shr lowerBitsToRemove) and (
                1 shl SeedConstant.WORD_INDEX_SIZE) - 1
    }

}

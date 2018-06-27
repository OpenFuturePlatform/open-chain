package io.openfuture.chain.crypto.seed.generator

import io.openfuture.chain.crypto.seed.PhraseLength
import io.openfuture.chain.crypto.seed.SeedConstant.BYTE_SIZE
import io.openfuture.chain.crypto.seed.SeedConstant.MULTIPLICITY_VALUE
import io.openfuture.chain.crypto.seed.SeedConstant.SECOND_BYTE_OFFSET
import io.openfuture.chain.crypto.seed.SeedConstant.THIRD_BYTE_OFFSET
import io.openfuture.chain.crypto.seed.SeedConstant.WORD_INDEX_SIZE
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
        val entropy = ByteArray(length.byteLength)
        SecureRandom().nextBytes(entropy)

        val wordIndexes = wordIndexes(entropy)
        val words = Array(wordIndexes.size) { seedWordRepository.findOneByIndex(wordIndexes[it]).value }
        return StringUtils.arrayToDelimitedString(words, org.apache.commons.lang3.StringUtils.SPACE)
    }

    private fun wordIndexes(entropy: ByteArray): IntArray {
        val entropySize = entropy.size * BYTE_SIZE

        val entropyWithChecksum = Arrays.copyOf(entropy, entropy.size + 1)
        entropyWithChecksum[entropy.size] = HashUtils.sha256(entropy)[0]

        val checksumLength = entropySize / MULTIPLICITY_VALUE
        val mnemonicLength = (entropySize + checksumLength) / WORD_INDEX_SIZE

        val wordIndexes = IntArray(mnemonicLength)
        var bitOffset = 0
        var wordIndex = 0
        while (wordIndex < mnemonicLength) {
            wordIndexes[wordIndex] = nextWordsIndex(entropyWithChecksum, bitOffset)
            bitOffset += WORD_INDEX_SIZE
            wordIndex++
        }
        return wordIndexes
    }

    private fun nextWordsIndex(bytes: ByteArray, offset: Int): Int {
        val skip = offset / BYTE_SIZE
        val lowerBitsToRemove = (MAX_BYTES_TO_READ * BYTE_SIZE - WORD_INDEX_SIZE) -
                (offset % BYTE_SIZE)

        val firstBytePart = bytes[skip].toInt() and BYTE_MASK shl DOUBLE_BYTE_SIZE
        val secondBytePart = bytes[skip + SECOND_BYTE_OFFSET].toInt() and BYTE_MASK shl BYTE_SIZE
        val thirdBytePart = if (lowerBitsToRemove < BYTE_SIZE) {
            bytes[skip + THIRD_BYTE_OFFSET].toInt() and BYTE_MASK
        } else {
            0
        }

        return ((firstBytePart or secondBytePart or thirdBytePart) shr lowerBitsToRemove) and (
                1 shl WORD_INDEX_SIZE) - 1
    }

}
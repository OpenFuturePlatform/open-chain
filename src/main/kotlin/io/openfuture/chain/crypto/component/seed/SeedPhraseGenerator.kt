package io.openfuture.chain.crypto.component.seed

import io.openfuture.chain.crypto.constants.SeedConstant.BYTE_SIZE
import io.openfuture.chain.crypto.constants.SeedConstant.DOUBLE_BYTE_SIZE
import io.openfuture.chain.crypto.constants.SeedConstant.SECOND_BYTE_OFFSET
import io.openfuture.chain.crypto.constants.SeedConstant.THIRD_BYTE_OFFSET
import io.openfuture.chain.crypto.constants.SeedConstant.WORD_INDEX_SIZE
import io.openfuture.chain.crypto.model.dictionary.PhraseLength
import io.openfuture.chain.crypto.repository.SeedWordRepository
import io.openfuture.chain.crypto.util.HashUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class SeedPhraseGenerator(
    private val seedWordRepository: SeedWordRepository
) {

    companion object {
        private const val BYTE_MASK = 0xff
        private const val MAX_BYTES_TO_READ = 3
        private const val WORD_INDEX_MASK = (1 shl WORD_INDEX_SIZE) - 1
    }


    fun createSeedPhrase(length: PhraseLength): String {
        val wordIndexes = wordIndexes(length)
        val words = Array(wordIndexes.size) { seedWordRepository.findOneByIndex(wordIndexes[it]).value }
        return words.joinToString(StringUtils.SPACE)
    }

    private fun wordIndexes(length: PhraseLength): IntArray {
        val entropy = ByteArray(length.entropyLength / BYTE_SIZE)
        SecureRandom().nextBytes(entropy)

        val entropyWithChecksum = Arrays.copyOf(entropy, entropy.size + 1)
        entropyWithChecksum[entropy.size] = HashUtils.sha256(entropy)[0]

        val wordIndexes = IntArray(length.value)
        var bitOffset = 0
        var wordIndex = 0
        while (wordIndex < length.value) {
            wordIndexes[wordIndex] = nextWordsIndex(entropyWithChecksum, bitOffset)
            bitOffset += WORD_INDEX_SIZE
            wordIndex++
        }
        return wordIndexes
    }

    /**
     * Method returns [WORD_INDEX_SIZE] bits of [bytes] from [offset] bits.
     *
     * firstBytePart contains second byte filled not zero exactly
     * secondBytePart contains third byte filled not zero exactly
     * thirdBytePart contains fourth byte filled not zero exactly
     *
     * After getting these variables they are summarized and shift right for lacking bits of right edge of byte.
     * Then it gets mask with eleven the one digits
     *
     * @param bytes byte array to get index from offset to offset + [WORD_INDEX_SIZE] bits in integer view
     *
     * @return index from offset to offset + [WORD_INDEX_SIZE] bit in integer view
     */
    private fun nextWordsIndex(bytes: ByteArray, offset: Int): Int {
        val skip = offset / BYTE_SIZE
        val lowerBitsToRemove = (MAX_BYTES_TO_READ * BYTE_SIZE - WORD_INDEX_SIZE) - (offset % BYTE_SIZE)

        val firstBytePart = bytes[skip].toInt() and BYTE_MASK shl DOUBLE_BYTE_SIZE
        val secondBytePart = bytes[skip + SECOND_BYTE_OFFSET].toInt() and BYTE_MASK shl BYTE_SIZE
        val thirdBytePart = if (lowerBitsToRemove < BYTE_SIZE) {
            bytes[skip + THIRD_BYTE_OFFSET].toInt() and BYTE_MASK
        } else {
            0
        }

        return ((firstBytePart or secondBytePart or thirdBytePart) shr lowerBitsToRemove) and WORD_INDEX_MASK
    }

}
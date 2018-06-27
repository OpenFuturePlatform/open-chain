package io.openfuture.chain.component.seed.validator

import io.openfuture.chain.component.seed.SeedConstant
import io.openfuture.chain.entity.SeedWord
import io.openfuture.chain.exception.SeedValidationException
import io.openfuture.chain.repository.SeedWordRepository
import io.openfuture.chain.util.HashUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

@Component
class SeedPhraseValidator(
        private val seedWordRepository: SeedWordRepository
) {

    companion object {
        private const val MULTIPLICITY_WITH_CHECKSUM_VALUE = 33
        private const val MAX_BYTE_SIZE_MOD = 7
        private const val OUT_OF_BYTE_SIZE = SeedConstant.WORD_INDEX_SIZE - SeedConstant.BYTE_SIZE
        private const val MIN_FIRST_BIT_INDEX_IN_THREE_BYTE = 6
        private const val SECOND_BYTE_SKIP_BIT_SIZE = 5
        private const val THIRD_BYTE_SKIP_BIT_SIZE = 13
    }

    fun validate(seedPhrase: String) {
        val seedPhraseWords = seedPhrase.split(StringUtils.SPACE)
        val seedWordIndexes = findWordIndexes(seedPhraseWords)
        validate(seedWordIndexes)
    }

    private fun validate(seedWordIndexes: IntArray) {
        val seedWordSize = seedWordIndexes.size
        val entropyPlusChecksumSize = seedWordSize * SeedConstant.WORD_INDEX_SIZE
        val entropySize = entropyPlusChecksumSize * SeedConstant.MULTIPLICITY_VALUE / MULTIPLICITY_WITH_CHECKSUM_VALUE
        val checksumSize = entropySize / SeedConstant.MULTIPLICITY_VALUE

        if (entropyPlusChecksumSize != entropySize + checksumSize) {
            throw SeedValidationException("Invalid word count = $seedWordSize")
        }

        val entropyWithChecksum = ByteArray((entropyPlusChecksumSize + MAX_BYTE_SIZE_MOD) / SeedConstant.BYTE_SIZE)
        wordIndexesToEntropyWithCheckSum(seedWordIndexes, entropyWithChecksum)
        val entropy = Arrays.copyOf(entropyWithChecksum, entropyWithChecksum.size - 1)
        val entropySha = HashUtils.sha256(entropy)[0]
        val mask = ((1 shl (SeedConstant.BYTE_SIZE - checksumSize)) - 1).inv().toByte()

        val lastByte = entropyWithChecksum[entropyWithChecksum.size - 1]
        if (entropySha xor lastByte and mask != 0.toByte()) {
            throw SeedValidationException("Invalid checksum for seed phrase")
        }
    }

    private fun findWordIndexes(seedPhraseWords: Collection<String>): IntArray
            = seedPhraseWords.map { getWord(it).index }.toIntArray()

    private fun getWord(wordValue: String): SeedWord {
        return seedWordRepository.findOneByValue(wordValue).orElseThrow {
            throw SeedValidationException("Word $wordValue not found")
        }
    }

    private fun wordIndexesToEntropyWithCheckSum(wordIndexes: IntArray, entropyWithChecksum: ByteArray) {
        var wordIndex = 0
        var entropyOffset = 0
        while (wordIndex < wordIndexes.size) {
            writeNextWordIndexToArray(entropyWithChecksum, wordIndexes[wordIndex], entropyOffset)
            wordIndex++
            entropyOffset += SeedConstant.WORD_INDEX_SIZE
        }
    }

    private fun writeNextWordIndexToArray(bytes: ByteArray, value: Int, offset: Int) {
        val byteSkip = offset / SeedConstant.BYTE_SIZE
        val bitSkip = offset % SeedConstant.BYTE_SIZE

        writeFirstByteToArray(bytes, byteSkip, bitSkip, value)
        writeSecondByteToArray(bytes, byteSkip, bitSkip, value)
        writeThirdByteToArray(bytes, byteSkip, bitSkip, value)
    }

    private fun writeFirstByteToArray(bytes: ByteArray, byteSkip: Int, bitSkip: Int, value: Int) {
        val firstValue = bytes[byteSkip]
        val toWrite = (value shr (OUT_OF_BYTE_SIZE + bitSkip)).toByte()
        bytes[byteSkip] = (firstValue or toWrite)
    }

    private fun writeSecondByteToArray(bytes: ByteArray, byteSkip: Int, bitSkip: Int, value: Int) {
        val valueInByte = bytes[byteSkip + 1]
        val i = SECOND_BYTE_SKIP_BIT_SIZE - bitSkip
        val toWrite = (if (i > 0) value shl i else value shr -i).toByte()
        bytes[byteSkip + SeedConstant.SECOND_BYTE_OFFSET] = (valueInByte or toWrite)
    }

    private fun writeThirdByteToArray(bytes: ByteArray, byteSkip: Int, bitSkip: Int, value: Int) {
        if (bitSkip >= MIN_FIRST_BIT_INDEX_IN_THREE_BYTE) {
            val lastByteIndex = byteSkip + SeedConstant.THIRD_BYTE_OFFSET
            val lastByteValue = bytes[lastByteIndex]
            val toWrite = (value shl (THIRD_BYTE_SKIP_BIT_SIZE - bitSkip)).toByte()
            bytes[lastByteIndex] = (lastByteValue or toWrite)
        }
    }

}

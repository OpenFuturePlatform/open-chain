package io.openfuture.chain.component.seed.validator

import io.openfuture.chain.component.seed.SeedConstant
import io.openfuture.chain.exception.SeedValidationException
import io.openfuture.chain.repository.SeedWordRepository
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component
import java.util.*
import kotlin.experimental.or

@Component
class SeedPhraseValidator(
        private val seedWordRepository: SeedWordRepository
) {

    companion object {
        private const val MULTIPLICITY_WITH_CHECKSUM_VALUE = 33
        private const val MAX_BYTE_SIZE_MOD = 7
        private const val OUT_OF_BYTE_SIZE = SeedConstant.WORD_INDEX_SIZE - SeedConstant.BYTE_SIZE
    }

    fun validate(seedPhrase: String) {
        val seedPhraseWords = seedPhrase.split(SeedConstant.SEED_PHRASE_SEPARATOR)
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
        val lastByte = entropyWithChecksum[entropyWithChecksum.size - 1]
        val entropySha = HashUtils.sha256(entropy)[0]
        val mask = ((1 shl SeedConstant.BYTE_SIZE - checksumSize) - 1).inv().toByte()

        if (entropySha.toInt() xor lastByte.toInt() and mask.toInt() != 0) {
            throw SeedValidationException("Invalid checksum for seed phrase")
        }
    }

    private fun findWordIndexes(split: Collection<String>): IntArray {
        val ms = split.size
        val result = IntArray(ms)
        for ((i, buffer) in split.withIndex()) {
            if (buffer.isEmpty()) {
                throw SeedValidationException("Phrase has excess whitespaces")
            }

            val word = seedWordRepository.findOneByValue(buffer)
            result[i] = word.index
        }
        return result
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
        writeThirdByteToArray(bytes, byteSkip, bitSkip, value)
    }

    private fun writeFirstByteToArray(bytes: ByteArray, byteSkip: Int, bitSkip: Int, value: Int) {
        val firstValue = bytes[byteSkip]
        val toWrite = (value shr (OUT_OF_BYTE_SIZE + bitSkip)).toByte()
        bytes[byteSkip] = (firstValue or toWrite)
    }

    private fun writeSecondByteToArray(bytes: ByteArray, byteSkip: Int, bitSkip: Int, value: Int) {
        val valueInByte = bytes[byteSkip + 1]
        val i = 5 - bitSkip
        val toWrite = (if (i > 0) value shl i else value shr -i).toByte()
        bytes[byteSkip + 1] = (valueInByte or toWrite)
    }

    private fun writeThirdByteToArray(bytes: ByteArray, byteSkip: Int, bitSkip: Int, value: Int) {
        if (bitSkip >= 6) {
            val lastByteIndex = byteSkip + 2
            val lastByteValue = bytes[lastByteIndex]
            val toWrite = (value shl (13 - bitSkip)).toByte()
            bytes[lastByteIndex] = (lastByteValue or toWrite)
        }
    }

}

package io.openfuture.chain.crypto.util

import org.apache.commons.lang3.StringUtils
import java.util.*


object Base58 {

    private val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val ENCODED_ZERO = ALPHABET[0]
    private val INDEXES = IntArray(128)


    init {
        INDEXES.fill(-1)
        for (i in ALPHABET.indices) {
            INDEXES[ALPHABET[i].toInt()] = i
        }
    }

    fun encode(bytes: ByteArray): String {
        if (bytes.isEmpty()) {
            return StringUtils.EMPTY
        }

        var leadingZerosCount = 0
        while (leadingZerosCount < bytes.size && bytes[leadingZerosCount].toInt() == 0) {
            ++leadingZerosCount
        }

        val result = Arrays.copyOf(bytes, bytes.size)

        val encoded = CharArray(result.size * 2)
        var outputStart = encoded.size
        var inputStart = leadingZerosCount
        while (inputStart < result.size) {
            encoded[--outputStart] = ALPHABET[divmod(result, inputStart, 256, 58).toInt()]
            if (result[inputStart].toInt() == 0) {
                ++inputStart
            }
        }

        while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart
        }

        while (--leadingZerosCount > 0) {
            encoded[--outputStart] = ENCODED_ZERO
        }

        return String(encoded, outputStart, encoded.size - outputStart)
    }

    fun encodeWithChecksum(bytes: ByteArray): String {
        val checkSum = HashUtils.doubleSha256(bytes)
        val extended = ByteArray(bytes.size + 4)
        System.arraycopy(bytes, 0, extended, 0, bytes.size)
        System.arraycopy(checkSum, 0, extended, bytes.size, 4)
        return encode(extended)
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) {
            return ByteArray(0)
        }

        val input58 = ByteArray(input.length)
        for (i in 0 until input.length) {
            val digit58 = if (input[i].toInt() in 0..127) {
                INDEXES[input[i].toInt()]
            } else {
                throw IllegalArgumentException("Invalid character ${input[i]} at $i")
            }

            input58[i] = digit58.toByte()
        }

        var leadingZerosCount = 0
        while (leadingZerosCount < input58.size && input58[leadingZerosCount].toInt() == 0) {
            ++leadingZerosCount
        }

        val decoded = ByteArray(input.length)
        var outputStart = decoded.size

        var inputStart = leadingZerosCount
        while (inputStart < input58.size) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256)
            if (input58[inputStart].toInt() == 0) {
                ++inputStart
            }
        }

        while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
            ++outputStart
        }

        return Arrays.copyOfRange(decoded, outputStart - leadingZerosCount, decoded.size)
    }

    fun decodeWithChecksum(input: String): ByteArray {
        val decoded = decode(input)
        if (decoded.size < 4)
            throw IllegalArgumentException("Input too short. Size: ${decoded.size}")
        val data = Arrays.copyOfRange(decoded, 0, decoded.size - 4)
        val checksum = Arrays.copyOfRange(decoded, decoded.size - 4, decoded.size)
        val actualChecksum = Arrays.copyOfRange(HashUtils.doubleSha256(data), 0, 4)
        if (!Arrays.equals(checksum, actualChecksum))
            throw IllegalArgumentException("Invalid checksum")
        return data
    }


    private fun divmod(number: ByteArray, firstDigit: Int, base: Int, divisor: Int): Byte {
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toInt() and 0xFF
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder.toByte()
    }

}
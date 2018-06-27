package io.openfuture.chain.util

import java.math.BigInteger

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
            return ""
        }

        var leadingZeros = 0
        while (leadingZeros < bytes.size && bytes[leadingZeros].toInt() == 0) {
            ++leadingZeros
        }

        val result = StringBuffer()
        var number = BigInteger(1, bytes)
        while (number > BigInteger.ZERO) {
            val r = number.divideAndRemainder(BigInteger.valueOf(58))
            number = r[0]
            result.append(ALPHABET[r[1].toInt()])
        }

        while (--leadingZeros > 0) {
            result.append(ENCODED_ZERO)
        }
        return result.reverse().toString()
    }

    fun encodeWithChecksum(bytes: ByteArray): String {
        val checkSum = HashUtils.generateHashBytes(bytes)
        val extended = ByteArray(bytes.size + 4)
        System.arraycopy(bytes, 0, extended, 0, bytes.size)
        System.arraycopy(checkSum, 0, extended, bytes.size, 4)
        return encode(extended)
    }

}
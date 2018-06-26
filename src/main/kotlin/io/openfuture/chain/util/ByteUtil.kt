package io.openfuture.chain.util

import io.openfuture.chain.domain.key.Hash
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.util.*
import kotlin.experimental.and

/**
 * @author Alexey Skadorva
 */
object ByteUtil {

    /**
     * Base58 - Encoding of BIP32 Keys
     */
    private val b58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val indexes58 = IntArray(128)

    fun toHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (i in bytes.indices) {
            sb.append(Integer.toString((bytes[i] and 0xff.toByte()) + 0x100, 16).substring(1))
        }
        return sb.toString()
    }

    fun fromHex(hex: String): ByteArray {
        return Hex.decode(hex)
    }

    fun reverseBytes(bytes: ByteArray): ByteArray {
        val buf = ByteArray(bytes.size)
        for (i in bytes.indices)
            buf[i] = bytes[bytes.size - 1 - i]
        return buf
    }

    init {
        for (i in indexes58.indices) {
            indexes58[i] = -1
        }
        for (i in b58.indices) {
            indexes58[b58[i].toInt()] = i
        }
    }

    fun toBase58(b: ByteArray): String {
        if (b.size == 0) {
            return ""
        }

        var lz = 0
        while (lz < b.size && b[lz].toInt() == 0) {
            ++lz
        }
        val s = StringBuffer()
        var n = BigInteger(1, b)
        while (n.compareTo(BigInteger.ZERO) > 0) {
            val r = n.divideAndRemainder(BigInteger.valueOf(58))
            n = r[0]
            val digit = b58[r[1].toInt()]
            s.append(digit)
        }
        while (lz > 0) {
            --lz
            s.append("1")
        }
        return s.reverse().toString()
    }

    fun toBase58WithChecksum(b: ByteArray): String {
        val cs = Hash.hash(b)
        val extended = ByteArray(b.size + 4)
        System.arraycopy(b, 0, extended, 0, b.size)
        System.arraycopy(cs, 0, extended, b.size, 4)
        return toBase58(extended)
    }

    @Throws(Exception::class)
    fun fromBase58WithChecksum(s: String): ByteArray {
        val b = fromBase58(s)
        if (b.size < 4) {
            throw Exception("Too short for checksum: " + s + " l:  " + b.size)
        }
        val cs = ByteArray(4)
        System.arraycopy(b, b.size - 4, cs, 0, 4)
        val data = ByteArray(b.size - 4)
        System.arraycopy(b, 0, data, 0, b.size - 4)
        val h = ByteArray(4)
        System.arraycopy(Hash.hash(data), 0, h, 0, 4)
        if (Arrays.equals(cs, h)) {
            return data
        }
        throw Exception("Checksum mismatch: $s")
    }

    @Throws(Exception::class)
    fun fromBase58(input: String): ByteArray {
        if (input.length == 0) {
            return ByteArray(0)
        }
        val input58 = ByteArray(input.length)
        // Transform the String to a base58 byte sequence
        for (i in 0 until input.length) {
            val c = input[i]

            var digit58 = -1
            if (c.toInt() >= 0 && c.toInt() < 128) {
                digit58 = indexes58[c.toInt()]
            }
            if (digit58 < 0) {
                throw Exception("Illegal character $c at $i")
            }

            input58[i] = digit58.toByte()
        }
        // Count leading zeroes
        var zeroCount = 0
        while (zeroCount < input58.size && input58[zeroCount].toInt() == 0) {
            ++zeroCount
        }
        // The encoding
        val temp = ByteArray(input.length)
        var j = temp.size

        var startAt = zeroCount
        while (startAt < input58.size) {
            val mod = divmod256(input58, startAt)
            if (input58[startAt].toInt() == 0) {
                ++startAt
            }

            temp[--j] = mod
        }
        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.size && temp[j].toInt() == 0) {
            ++j
        }

        return copyOfRange(temp, j - zeroCount, temp.size)
    }

    private fun divmod256(number58: ByteArray, startAt: Int): Byte {
        var remainder = 0
        for (i in startAt until number58.size) {
            val digit58 = number58[i].toInt() and 0xFF
            val temp = remainder * 58 + digit58
            number58[i] = (temp / 256).toByte()
            remainder = temp % 256
        }
        return remainder.toByte()
    }

    private fun copyOfRange(source: ByteArray, from: Int, to: Int): ByteArray {
        val range = ByteArray(to - from)
        System.arraycopy(source, from, range, 0, range.size)
        return range
    }
}
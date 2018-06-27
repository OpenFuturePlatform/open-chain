package io.openfuture.chain.util

import java.math.BigInteger

object ByteUtils {

    private val base58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val indexes58 = IntArray(128)

    init {
        indexes58.fill(-1)
        for (i in base58.indices) {
            indexes58[base58[i].toInt()] = i
        }
    }

    fun toBase58(b: ByteArray): String {
        if (b.isEmpty()) {
            return ""
        }

        var lz = 0
        while (lz < b.size && b[lz].toInt() == 0) {
            ++lz
        }
        val s = StringBuffer()
        var n = BigInteger(1, b)
        while (n > BigInteger.ZERO) {
            val r = n.divideAndRemainder(BigInteger.valueOf(58))
            n = r[0]
            val digit = base58[r[1].toInt()]
            s.append(digit)
        }
        while (lz > 0) {
            --lz
            s.append("1")
        }
        return s.reverse().toString()
    }

    fun toBase58WithChecksum(b: ByteArray): String {
        val cs = HashUtils.generateHashBytes(b)
        val extended = ByteArray(b.size + 4)
        System.arraycopy(b, 0, extended, 0, b.size)
        System.arraycopy(cs, 0, extended, b.size, 4)
        return toBase58(extended)
    }

}
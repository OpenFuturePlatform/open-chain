package io.openfuture.chain.domain.key

import org.bouncycastle.crypto.digests.RIPEMD160Digest
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author Alexey Skadorva
 */
class Hash(private val input: ByteArray) {
    private var rounds = 50000
    private var func = SHA256

    constructor(input: String, rounds: Int, func: String) : this(input.toByteArray()) {
        this.rounds = rounds
        this.func = func
    }

    constructor(input: String) : this(input.toByteArray()) {}

    @Throws(Exception::class)
    fun hash(): ByteArray? {
        return if (func == SHA256) {
            sha256Hash(rounds)
        } else if (func == HmacSHA256) {
            hmacHash(rounds)
        } else {
            throw Error("Hashing function not supported")
        }
    }

    @Throws(Exception::class)
    private fun hmacHash(rounds: Int): ByteArray {
        val key = SecretKeySpec(input, HmacSHA256)
        val mac = Mac.getInstance(HmacSHA256)
        mac.init(key)
        var last = input
        for (i in 1..rounds) {
            last = mac.doFinal(last)
        }
        return last
    }

    /**
     * Used to generate a maser's key hash (using "Bitcoin seed" string as key)
     *
     * @param keyStr - hashing key
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getHmacSHA512(keyStr: String): ByteArray {
        val key = SecretKeySpec(keyStr.toByteArray(), HmacSHA512)
        val mac = Mac.getInstance(HmacSHA512, "BC")
        mac.init(key)
        return mac.doFinal(this.input)
    }

    @Throws(Exception::class)
    fun getHmacSHA256(keyBytes: ByteArray): ByteArray {
        val key = SecretKeySpec(keyBytes, HmacSHA256)
        val mac = Mac.getInstance(HmacSHA256, "BC")
        mac.init(key)
        return mac.doFinal(this.input)
    }

    @Throws(Exception::class)
    private fun sha256Hash(rounds: Int): ByteArray? {
        val md = MessageDigest.getInstance(SHA256)
        var last: ByteArray? = null
        for (i in 1..rounds) {
            md.update(last ?: input)
            last = md.digest()
        }
        return last
    }

    /**
     * BIP32 Extended Key Public hash
     */
    fun keyHash(): ByteArray {
        val ph = ByteArray(20)
        try {
            val sha256 = MessageDigest.getInstance(SHA256).digest(input)
            val digest = RIPEMD160Digest()
            digest.update(sha256, 0, sha256.size)
            digest.doFinal(ph, 0)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        return ph
    }

    fun sha256(): ByteArray {
        var out = ByteArray(0)
        try {
            out = MessageDigest.getInstance(SHA256).digest(input)
        } catch (e: NoSuchAlgorithmException) {

        }

        return out
    }

    companion object {

        private val SHA256 = "SHA-256"
        private val HmacSHA256 = "HmacSHA256"
        private val HmacSHA512 = "HmacSHA512"

        /**
         * Used by Base58 with Checksum encoding for extended keys
         */
        @JvmOverloads
        fun hash(data: ByteArray, offset: Int = 0, len: Int = data.size): ByteArray {
            try {
                val a = MessageDigest.getInstance(SHA256)
                a.update(data, offset, len)
                return a.digest(a.digest())
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

        }
    }
}
